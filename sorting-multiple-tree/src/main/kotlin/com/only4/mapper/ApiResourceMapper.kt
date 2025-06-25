package com.only4.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.only4.entity.ApiResource
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface ApiResourceMapper : BaseMapper<ApiResource> {

    /**
     * 根据父ID查询子资源
     */
    fun findByParentId(
        @Param("parentId") parentId: String,
        @Param("tableSelector") tableSelector: Int
    ): List<ApiResource>

    /**
     * 查找所有根节点（没有父节点的节点）
     */
    fun findRoots(@Param("tableSelector") tableSelector: Int): List<ApiResource>

    /**
     * 根据路径查询
     */
    fun findByPathStartsWith(@Param("path") path: String, @Param("tableSelector") tableSelector: Int): List<ApiResource>

    /**
     * 查询指定表中的所有记录
     */
    fun findAllFromTable(@Param("tableSelector") tableSelector: Int): List<ApiResource>
}
