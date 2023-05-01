package xyz.hyrio.common.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import xyz.hyrio.common.tool.PagerArgs;

import java.util.List;

@Schema(description = "分页通用返回值")
public class PagedCommonVo<T> extends CommonVo<List<T>> {
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    @Schema(description = "当前页")
    private int currentPage;
    @Schema(description = "每页数量")
    private int pageSize;
    @Schema(description = "总数量")
    private int totalCount;

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public String toString() {
        return "PagedCommonVo{" +
               "currentPage=" + currentPage +
               ", pageSize=" + pageSize +
               ", totalCount=" + totalCount +
               '}';
    }

    private PagedCommonVo(List<T> data, PagerArgs pagerArgs) {
        super(200, SUCCESS_PROMPT_TEXT, data);
        this.currentPage = pagerArgs.getCurrentPage();
        this.pageSize = pagerArgs.getPageSize();
        this.totalCount = pagerArgs.getTotalCount();
    }

    public static <T> PagedCommonVo<T> of(List<T> data, PagerArgs pagerArgs) {
        return new PagedCommonVo<>(data, pagerArgs);
    }
}
