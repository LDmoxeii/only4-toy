package com.only4.config

import com.only4.entity.ApiResource
import com.only4.synchronizer.DefaultSortingTreeSynchronizer
import com.only4.synchronizer.SortingTreeSynchronizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SynchronizerConfig {

    @Bean
    fun apiResourceSynchronizer(): SortingTreeSynchronizer<String, ApiResource.ApiResourceInfo> {
        return DefaultSortingTreeSynchronizer()
    }

}
