# jedis-ext
扩展jedis功能

===============
目前主要是实现了redis集群模式下的批量操作功能，需要注意的是：
 * 由于集群模式存在节点的动态添加删除，且client不能实时感知（只有在执行命令时才可能知道集群发生变更），
 * 因此，该实现不保证一定成功，建议在批量操作之前调用 refreshCluster() 方法重新获取集群信息。
 * 应用需要保证不论成功还是失败都会调用close() 方法，否则可能会造成泄露。
 * 如果失败需要应用自己去重试，因此每个批次执行的命令数量需要控制。防止失败后重试的数量过多。
 * 基于以上说明，建议在集群环境较稳定（增减节点不会过于频繁）的情况下使用，且允许失败或有对应的重试策略。
 
 其调用方式如下：
 ``` java
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
 ```
