import Treeselect from '@riophae/vue-treeselect'

// Keep the vendor's selection behavior while using Chinese default prompts.
export default {
  extends: Treeselect,
  props: {
    clearAllText: { type: String, default: '清空全部' },
    clearValueText: { type: String, default: '清空选择' },
    loadingText: { type: String, default: '加载中...' },
    noChildrenText: { type: String, default: '暂无下级选项' },
    noOptionsText: { type: String, default: '暂无可选项' },
    noResultsText: { type: String, default: '暂无匹配结果' },
    retryText: { type: String, default: '重试' },
    searchPromptText: { type: String, default: '请输入搜索内容' },
    placeholder: { type: String, default: '请选择' }
  }
}
