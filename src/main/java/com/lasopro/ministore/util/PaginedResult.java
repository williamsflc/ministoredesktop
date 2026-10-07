
package com.lasopro.ministore.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author williams
 */
public class PaginedResult implements Serializable{
    
    private int total;
    private int pageSize;
    private int totalPages;
    private int page;
    private List<Map<String,Object>> data;

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    
    
    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }
    
    
    
}
