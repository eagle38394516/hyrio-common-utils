package xyz.hyrio.common.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import xyz.hyrio.common.exception.request.AuthorizationException;

import java.io.IOException;

import static org.springframework.util.StringUtils.hasText;

/**
 * 用于生成token的工具类。
 */
public class TokenTool {
    private final ObjectMapper objectMapper;
    private final EncryptionTool encryptionTool;

    private final String tokenPrefix;
    private final int tokenPrefixLength;

    public TokenTool(
            ObjectMapper objectMapper,
            EncryptionTool encryptionTool,
            String tokenPrefix
    ) {
        this.objectMapper = objectMapper;
        this.encryptionTool = encryptionTool;
        this.tokenPrefix = tokenPrefix;
        this.tokenPrefixLength = tokenPrefix.length();
    }

    /**
     * 生成token。
     *
     * @param userInfo Token中包含的用户信息。
     * @return 生成的token（不含前缀）。
     */
    public <UserInfoType> String generate(UserInfoType userInfo) throws JsonProcessingException {
        return encryptionTool.encryptToString(objectMapper.writeValueAsString(userInfo));
    }

    public String addPrefix(String token) {
        return tokenPrefix + token;
    }

    private String getInvalidTokenExceptionMessage(String errorMessage) {
        return "invalid token (" + errorMessage + ")";
    }

    public String stripPrefix(String token) {
        if (!hasText(token)) {
            throw new AuthorizationException("token is empty");
        }
        if (!token.startsWith(tokenPrefix)) {
            throw new AuthorizationException(getInvalidTokenExceptionMessage("wrong prefix"));
        }
        return token.substring(tokenPrefixLength);
    }

    public <UserInfoType> UserInfoType parse(String tokenWithoutPrefix, Class<UserInfoType> userInfoClass) {
        try {
            return objectMapper.readValue(encryptionTool.decryptToBytes(tokenWithoutPrefix), userInfoClass);
        } catch (IOException e) {
            throw new AuthorizationException(getInvalidTokenExceptionMessage("unable to deserialize"), e);
        }
    }
}
