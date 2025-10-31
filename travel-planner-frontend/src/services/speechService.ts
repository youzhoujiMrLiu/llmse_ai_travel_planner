// src/services/speechService.ts

/**
 * 科大讯飞语音识别服务（需要额外配置）
 * 当前未启用，使用 Web Speech API 代替
 */
// export class XFYunSpeechRecognition {
//   // 科大讯飞语音识别实现
//   // 需要安装 crypto-js: npm install crypto-js
//   // 需要后端提供签名接口或前端实现签名逻辑
// }

/**
 * 简化的语音识别服务（使用 Web Speech API）
 * 作为科大讯飞的备选方案
 */
export class WebSpeechRecognition {
  private recognition: any

  constructor() {
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
    if (!SpeechRecognition) {
      throw new Error('浏览器不支持语音识别')
    }

    this.recognition = new SpeechRecognition()
    this.recognition.lang = 'zh-CN'
    this.recognition.continuous = true
    this.recognition.interimResults = true
  }

  startRecognition(onResult: (text: string) => void, onError: (error: string) => void) {
    this.recognition.onresult = (event: any) => {
      let transcript = ''
      for (let i = event.resultIndex; i < event.results.length; i++) {
        transcript += event.results[i][0].transcript
      }
      onResult(transcript)
    }

    this.recognition.onerror = (event: any) => {
      onError('识别失败: ' + event.error)
    }

    this.recognition.start()
  }

  stopRecognition() {
    this.recognition.stop()
  }
}
