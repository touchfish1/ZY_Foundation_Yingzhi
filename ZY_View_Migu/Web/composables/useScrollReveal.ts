export function useScrollReveal() {
  const observer = ref<IntersectionObserver | null>(null)

  function observe(el: Element, delay = 0) {
    if (!observer.value) {
      observer.value = new IntersectionObserver(
        (entries) => {
          entries.forEach(entry => {
            if (entry.isIntersecting) {
              setTimeout(() => {
                entry.target.classList.add('revealed')
              }, delay)
              observer.value?.unobserve(entry.target)
            }
          })
        },
        { threshold: 0.1, rootMargin: '0px 0px -40px 0px' }
      )
    }
    observer.value.observe(el)
  }

  function disconnect() {
    observer.value?.disconnect()
    observer.value = null
  }

  onMounted(() => { /* ready */ })
  onUnmounted(() => disconnect())

  return { observe, disconnect }
}
