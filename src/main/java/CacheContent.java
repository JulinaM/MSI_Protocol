class CacheContent {
    public enum CacheState {
        INVALID, SHARED, MODIFIED
    }

    int[] value;
    CacheState cacheState;


    CacheContent(int[] value, CacheState cacheState) {
        this.value = value;
        this.cacheState = cacheState;
    }

}
