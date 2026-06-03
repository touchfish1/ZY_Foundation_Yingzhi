import { useDialog } from 'naive-ui'
import type { DialogOptions } from 'naive-ui'

// 确认对话框组合式函数：封装 Naive UI 的 dialog.warning，返回 Promise<boolean>
export function useConfirm() {
  console.log('[Composable] useConfirm')
  const dialog = useDialog()

  // 弹出确认对话框，用户点击"确定"返回 true，点击"取消"返回 false
  function confirm(options: DialogOptions): Promise<boolean> {
    return new Promise((resolve) => {
      dialog.warning({
        title: options.title || '确认操作',
        content: options.content || '确定要执行此操作吗？',
        positiveText: options.positiveText || '确定',
        negativeText: options.negativeText || '取消',
        onPositiveClick: () => resolve(true),
        onNegativeClick: () => resolve(false)
      })
    })
  }

  return { confirm }
}
