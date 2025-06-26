import Vue from 'vue'
import Vuex from 'vuex'
import axios from 'axios'
import {deleteResource, getDifferences, getResourceTree, updateResource} from '@/api/resource'

Vue.use(Vuex)

export default new Vuex.Store({
    state: {
        sourceSelector: 1,
        targetSelector: 2,
        sourceResources: [],
        targetResources: [],
        differences: [],
        selectedResources: []
    },
    mutations: {
        SET_SOURCE_SELECTOR(state, selector) {
            state.sourceSelector = selector
        },
        SET_TARGET_SELECTOR(state, selector) {
            state.targetSelector = selector
        },
        SET_SOURCE_RESOURCES(state, resources) {
            state.sourceResources = resources
        },
        SET_TARGET_RESOURCES(state, resources) {
            state.targetResources = resources
        },
        SET_DIFFERENCES(state, differences) {
            state.differences = differences
        },
        SET_SELECTED_RESOURCES(state, resources) {
            state.selectedResources = resources
        },
        ADD_SELECTED_RESOURCE(state, resource) {
            state.selectedResources.push(resource)
        },
        REMOVE_SELECTED_RESOURCE(state, resourceKey) {
            state.selectedResources = state.selectedResources.filter(r => r !== resourceKey)
        },
        CLEAR_SELECTED_RESOURCES(state) {
            state.selectedResources = []
        },
        SWITCH_SELECTORS(state) {
            const temp = state.sourceSelector
            state.sourceSelector = state.targetSelector
            state.targetSelector = temp
        }
    },
    actions: {
        // 切换源表和目标表
        switchSourceTarget({commit, dispatch}) {
            commit('SWITCH_SELECTORS')
            dispatch('loadSourceResources')
            dispatch('loadTargetResources')
        },

        // 加载源表资源
        async loadSourceResources({commit, state, dispatch}) {
            try {
                const response = await getResourceTree(state.sourceSelector)
                commit('SET_SOURCE_RESOURCES', response.data)
                dispatch('loadDifferences')
            } catch (error) {
                console.error('加载源表资源失败:', error)
            }
        },

        // 加载目标表资源
        async loadTargetResources({commit, state, dispatch}) {
            try {
                const response = await getResourceTree(state.targetSelector)
                commit('SET_TARGET_RESOURCES', response.data)
                dispatch('loadDifferences')
            } catch (error) {
                console.error('加载目标表资源失败:', error)
            }
        },

        // 加载差异数据
        async loadDifferences({commit, state}) {
            if (state.sourceResources.length && state.targetResources.length) {
                try {
                    const response = await getDifferences(state.sourceSelector, state.targetSelector)
                    commit('SET_DIFFERENCES', response.data)
                } catch (error) {
                    console.error('加载差异数据失败:', error)
                }
            }
        },

        // 同步选中的资源
        async syncResources({state}) {
            if (state.selectedResources.length === 0) return

            try {
                await axios.post('/resources/sync', state.selectedResources, {
                    params: {
                        sourceSelector: state.sourceSelector,
                        targetSelector: state.targetSelector
                    }
                })

                // 重新加载数据
                this.dispatch('loadTargetResources')
                this.dispatch('loadDifferences')
            } catch (error) {
                console.error('同步资源失败:', error)
            }
        },

        // 更新资源
        async updateResource({dispatch}, {resource, selector}) {
            try {
                await updateResource(resource.key, resource, selector)

                // 重新加载数据
                if (selector === this.state.sourceSelector) {
                    dispatch('loadSourceResources')
                } else {
                    dispatch('loadTargetResources')
                }
            } catch (error) {
                console.error('更新资源失败:', error)
            }
        },

        // 创建资源
        async createResource({dispatch}, {resource, selector}) {
            try {
                // 创建资源对象的副本，避免修改原对象
                const resourceToSend = {...resource}

                // 移除relativeSortIndex字段，后端API可能不需要这个字段
                if ('relativeSortIndex' in resourceToSend) {
                    // 如果sort值为0且存在relativeSortIndex，使用relativeSortIndex作为sort值
                    if (resourceToSend.sort === 0 && resourceToSend.relativeSortIndex) {
                        resourceToSend.sort = resourceToSend.relativeSortIndex
                    }
                    delete resourceToSend.relativeSortIndex
                }

                // 确保sort字段有值
                if (!resourceToSend.sort && resourceToSend.sort !== 0) {
                    resourceToSend.sort = 1 // 默认值
                }

                // 确保sort值大于0
                if (resourceToSend.sort <= 0) {
                    resourceToSend.sort = 1
                }

                await axios.post('/resources', resourceToSend, {
                    params: {selector}
                })

                // 重新加载数据
                if (selector === this.state.sourceSelector) {
                    dispatch('loadSourceResources')
                } else {
                    dispatch('loadTargetResources')
                }
            } catch (error) {
                console.error('创建资源失败:', error)
            }
        },

        // 删除资源
        async deleteResource({dispatch}, {id, selector}) {
            try {
                await deleteResource(id, selector)

                // 重新加载数据
                if (selector === this.state.sourceSelector) {
                    dispatch('loadSourceResources')
                } else {
                    dispatch('loadTargetResources')
                }
            } catch (error) {
                console.error('删除资源失败:', error)
            }
        },

        // 更新资源状态
        async updateResourceStatus({dispatch}, {id, activeStatus, selector}) {
            try {
                await axios.patch(`/resources/${id}/status`, {}, {
                    params: {
                        selector,
                        activeStatus
                    }
                })

                // 重新加载数据
                if (selector === this.state.sourceSelector) {
                    dispatch('loadSourceResources')
                } else {
                    dispatch('loadTargetResources')
                }
            } catch (error) {
                console.error('更新资源状态失败:', error)
            }
        },

        // 批量更新状态
        async batchUpdateStatus({state, dispatch}, {activeStatus, selector}) {
            if (state.selectedResources.length === 0) return

            try {
                await axios.patch('/resources/batch/status', state.selectedResources, {
                    params: {
                        selector,
                        activeStatus
                    }
                })

                // 重新加载数据
                if (selector === state.sourceSelector) {
                    dispatch('loadSourceResources')
                } else {
                    dispatch('loadTargetResources')
                }
            } catch (error) {
                console.error('批量更新状态失败:', error)
            }
        }
    },
    getters: {
        syncStatus: (state) => (resourceKey) => {
            const difference = state.differences.find(d => d.node.key === resourceKey)
            if (!difference) return 'SAME'
            return difference.syncType
        },
        isSelected: (state) => (resourceKey) => {
            return state.selectedResources.includes(resourceKey)
        }
    }
})
