local http = require "resty.http"
local cjson = require "cjson"  -- JSON library for parsing response
local httpc = http.new()

-- Function to validate the access token with ms-auth
local function validate_access_token(access_token)
    local res, err = httpc:request_uri("http://auth_service/validate-token", {
        method = "POST",
        headers = {
            ["Authorization"] = access_token
        }
    })
    return res, err
end

-- Function to refresh the access token with ms-auth
local function refresh_access_token(refresh_token)
    local res, err = httpc:request_uri("http://auth_service/refresh-token", {
        method = "POST",
        headers = {
            ["Authorization"] = "Bearer " .. refresh_token
        }
    })
    return res, err
end

-- Function to parse JSON response and extract user info
local function extract_user_info(res_body)
    local user_info = cjson.decode(res_body)
    return user_info.user_id, user_info.permissions
end

-- Step 1: Try to validate the access token
local access_token = ngx.var.http_authorization
local res, err = validate_access_token(access_token)

-- Step 2: Handle different responses from the auth service
if res then
    if res.status == 200 then
        -- Token is valid, extract user info from the response body
        local user_id, permissions = extract_user_info(res.body)

        -- Set user info in NGINX headers for downstream services
        ngx.req.set_header("X-User-ID", user_id)
        ngx.req.set_header("X-Permissions", permissions)

    elseif res.status == 401 then
        -- Access token is expired, try to refresh it
        local refresh_token = ngx.var.cookie_refresh_token  -- Or retrieve from another source
        local refresh_res, refresh_err = refresh_access_token(refresh_token)

        -- If refresh is successful, retry the original request with new access token
        if refresh_res and refresh_res.status == 200 then
            local new_token = refresh_res.body  -- Assuming the token is directly in the body

            -- Set the new token in the request header and proceed
            ngx.req.set_header("Authorization", "Bearer " .. new_token)

            -- Retry validation with the new access token
            res, err = validate_access_token("Bearer " .. new_token)
            if res and res.status == 200 then
                user_id, permissions = extract_user_info(res.body)
                ngx.req.set_header("X-User-ID", user_id)
                ngx.req.set_header("X-Permissions", permissions)
            else
                ngx.status = 401
                ngx.say("Unauthorized: Token validation failed after refresh")
                ngx.exit(401)
            end

        else
            -- If refresh fails, return 401 Unauthorized
            ngx.status = 401
            ngx.say("Unauthorized: Failed to refresh token")
            ngx.exit(401)
        end

    elseif res.status == 406 then
        -- Handle 406 Not Acceptable if ms-auth indicates an unacceptable request
        ngx.status = 406
        ngx.say("Not Acceptable: The request parameters or headers are invalid.")
        ngx.exit(406)

    else
        -- Handle other unexpected errors from ms-auth
        ngx.status = 500
        ngx.say("Internal Server Error: Unexpected response from auth service")
        ngx.exit(500)
    end

else
    -- If we can't reach ms-auth at all
    ngx.status = 500
    ngx.say("Internal Server Error: Unable to connect to auth service")
    ngx.exit(500)
end
