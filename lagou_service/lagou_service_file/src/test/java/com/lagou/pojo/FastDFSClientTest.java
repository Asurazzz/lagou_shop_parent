package com.lagou.pojo;

import com.lagou.util.FastDFSClient;
import org.junit.Test;

public class FastDFSClientTest {

    @Test
    public void test1() throws Exception {
        System.out.println(FastDFSClient.getTrackerServer());
        System.out.println(FastDFSClient.getStoreClient());
    }
}
