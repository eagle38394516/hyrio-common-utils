package xyz.hyrio.common.tool;

import lombok.Data;

import java.util.List;

/**
 * Pager arguments for database results.
 *
 * @author Hyrio 2021/11/12 16:37
 */
@Data
public class PagerArgs {
    private static final int DEFAULT_PAGE_SIZE = 10;

    private int totalCount;
    private final int currentPage;
    private final int pageSize;

    public PagerArgs(Integer currentPage, Integer pageSize) {
        this.currentPage = currentPage == null || currentPage <= 0 ? 1 : currentPage;
        this.pageSize = pageSize == null || pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    public String getSqlLimit() {
        return pageSize > 0 ? String.format("LIMIT %d, %d", (currentPage - 1) * pageSize, pageSize) : "";
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> handlePagedResultFromDb(List<Object> res) {
        List<T> ret = (List<T>) res.get(0);
        this.totalCount = ((List<Integer>) res.get(1)).get(0);
        return ret;
    }

    public <T> List<T> getPagedList(List<T> data) {
        totalCount = data.size();
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = currentPage * pageSize;
        if (fromIndex >= totalCount) {
            return List.of();
        }
        return data.subList(fromIndex, Math.min(toIndex, totalCount));
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> unwrapPagedResult(List<Object> res) {
        return (List<T>) res.get(0);
    }
}
