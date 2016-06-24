/**
 * Copyright: Copyright (c) 2015 
 * 
 * @author youaremoon
 * @date 2016年6月25日
 * @version V1.0
 */
package com.yam.redis;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

/**
 * @Description: TODO
 * @author youaremoon
 * @date 2016年6月25日 上午1:01:21
 *
 */
public class JedisClusterPipelineTest {

	@Test
	public void test() {
		Set<HostAndPort> nodes = new HashSet<HostAndPort>();
        nodes.add(new HostAndPort("127.0.0.1", 9379));
        nodes.add(new HostAndPort("127.0.0.1", 9380));

        JedisCluster jc = new JedisCluster(nodes);

        long s = System.currentTimeMillis();

        JedisClusterPipeline jcp = JedisClusterPipeline.pipelined(jc);
        jcp.refreshCluster();
        List<Object> batchResult = null;
        try {
            // batch write
            for (int i = 0; i < 10000; i++) {
                jcp.set("k" + i, "v1" + i);
            }
            jcp.sync();

            // batch read
            for (int i = 0; i < 10000; i++) {
                jcp.get("k" + i);
            }
            batchResult = jcp.syncAndReturnAll();
        } finally {
            jcp.close();
        }

        // output time 
        long t = System.currentTimeMillis() - s;
        System.out.println(t);

        System.out.println(batchResult.size());

        // 实际业务代码中，close要在finally中调，这里之所以没这么写，是因为懒
        try {
			jc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
