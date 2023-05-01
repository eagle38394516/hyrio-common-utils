package xyz.hyrio.common.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "通用返回值")
public class CommonVo<T> {
    @Schema(description = "状态码")
    private int code;
    @Schema(description = "提示信息")
    private String message;
    @Schema(description = "数据")
    private T data;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    @Override
    public String toString() {
        return "CommonVo{" +
               "code=" + code +
               ", message='" + message + '\'' +
               ", data=" + data +
               '}';
    }

    public CommonVo() {
    }

    public CommonVo(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static final String SUCCESS_PROMPT_TEXT = "success";

    public static <T> CommonVo<T> success() {
        return new CommonVo<>(200, SUCCESS_PROMPT_TEXT, null);
    }

    public static <T> CommonVo<T> success(T data) {
        return new CommonVo<>(200, SUCCESS_PROMPT_TEXT, data);
    }

    public static <T> CommonVo<T> of(int code, String msg) {
        return new CommonVo<>(code, msg, null);
    }

    public static <T> CommonVo<T> of(int code, String msg, T data) {
        return new CommonVo<>(code, msg, data);
    }
}
