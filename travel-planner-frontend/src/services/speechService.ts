// src/services/speechService.ts

/**
 * 科大讯飞语音识别服务（通过后端 WebSocket 代理）
 * ✅ 安全: API Key 完全在后端,前端无法获取
 */
export class XFYunSpeechRecognition {
  private ws: WebSocket | null = null
  private isRecording = false
  private mediaRecorder: MediaRecorder | null = null
  private audioContext: AudioContext | null = null
  private audioStream: MediaStream | null = null

  /**
   * 开始语音识别
   */
  async startRecognition(
    onResult: (text: string, isFinal: boolean) => void,
    onError: (error: string) => void
  ): Promise<void> {
    if (this.isRecording) {
      console.warn('已经在录音中')
      return
    }

    try {
      // 获取麦克风权限
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
      this.audioStream = stream
      
      // 创建AudioContext
      this.audioContext = new AudioContext({ sampleRate: 16000 })
      const source = this.audioContext.createMediaStreamSource(stream)
      
      // 连接到后端 WebSocket 代理（而不是直接连接科大讯飞）
      const backendWsUrl = 'ws://localhost:8080/api/speech/websocket'
      this.ws = new WebSocket(backendWsUrl)
      
      this.ws.onopen = () => {
        console.log('✅ 已连接到后端语音识别服务')
        this.isRecording = true
        
        // 发送开始信号给后端
        this.ws!.send(JSON.stringify({ type: 'start' }))
        
        // 开始录音并发送音频数据
        this.startRecordingAndSend(source, stream)
      }
      
      this.ws.onmessage = (event) => {
        const data = JSON.parse(event.data)
        
        if (data.code !== 0) {
          onError(`识别错误: ${data.message}`)
          this.stopRecognition()
          return
        }
        
        if (data.data && data.data.result) {
          const result = data.data.result
          let text = ''
          
          result.ws.forEach((ws: any) => {
            ws.cw.forEach((cw: any) => {
              text += cw.w
            })
          })
          
          const isFinal = data.data.status === 2
          onResult(text, isFinal)
          
          if (isFinal) {
            console.log('✅ 识别完成')
            this.stopRecognition()
          }
        }
      }
      
      this.ws.onerror = (error) => {
        console.error('❌ WebSocket错误:', error)
        onError('WebSocket连接失败')
        this.stopRecognition()
      }
      
      this.ws.onclose = () => {
        console.log('WebSocket连接关闭')
        this.isRecording = false
      }
      
    } catch (error: any) {
      onError(error.message || '无法获取麦克风权限')
    }
  }

  /**
   * 开始录音并发送音频数据到后端
   */
  private startRecordingAndSend(source: MediaStreamAudioSourceNode, stream: MediaStream): void {
    // 创建ScriptProcessor处理音频数据
    const processor = this.audioContext!.createScriptProcessor(4096, 1, 1)
    
    source.connect(processor)
    processor.connect(this.audioContext!.destination)
    
    processor.onaudioprocess = (e) => {
      if (!this.isRecording || !this.ws || this.ws.readyState !== WebSocket.OPEN) {
        return
      }
      
      const inputData = e.inputBuffer.getChannelData(0)
      const outputData = new Int16Array(inputData.length)
      
      // 转换为16位PCM
      for (let i = 0; i < inputData.length; i++) {
        const sample = inputData[i] || 0
        const s = Math.max(-1, Math.min(1, sample))
        outputData[i] = s < 0 ? s * 0x8000 : s * 0x7FFF
      }
      
      // 直接发送二进制数据给后端（后端会转发给科大讯飞）
      this.ws!.send(outputData.buffer)
    }
    
    // 停止时断开
    stream.getTracks().forEach(track => {
      track.onended = () => {
        processor.disconnect()
      }
    })
  }

  /**
   * 停止语音识别
   */
  stopRecognition(): void {
    console.log('🛑 停止录音')
    
    // 停止音频流
    if (this.audioStream) {
      this.audioStream.getTracks().forEach(track => track.stop())
      this.audioStream = null
    }
    
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      // 发送结束信号给后端
      this.ws.send(JSON.stringify({ type: 'end' }))
      
      this.ws.close()
    }
    
    this.isRecording = false
    this.ws = null
    
    if (this.audioContext) {
      this.audioContext.close()
      this.audioContext = null
    }
  }
}

/**
 * 简化的语音识别服务（使用 Web Speech API）
 * 作为科大讯飞的备选方案
 */
export class WebSpeechRecognition {
  private recognition: any
  private isRecording = false
  private finalTranscript = ''

  constructor() {
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
    if (!SpeechRecognition) {
      throw new Error('浏览器不支持语音识别')
    }

    this.recognition = new SpeechRecognition()
    this.recognition.lang = 'zh-CN'
    this.recognition.continuous = false  // ✅ 改为false，点击停止时才结束
    this.recognition.interimResults = true  // 显示中间结果
  }

  startRecognition(
    onResult: (text: string, isFinal: boolean) => void,
    onError: (error: string) => void
  ): void {
    if (this.isRecording) {
      console.warn('已经在录音中')
      return
    }

    this.isRecording = true
    this.finalTranscript = ''

    this.recognition.onresult = (event: any) => {
      let interimTranscript = ''
      
      for (let i = event.resultIndex; i < event.results.length; i++) {
        const transcript = event.results[i][0].transcript
        if (event.results[i].isFinal) {
          this.finalTranscript += transcript
          console.log('📝 最终片段:', transcript)
        } else {
          interimTranscript += transcript
        }
      }
      
      const fullText = this.finalTranscript + interimTranscript
      console.log('🎤 识别结果:', fullText, '(最终:', this.finalTranscript, ', 临时:', interimTranscript, ')')
      onResult(fullText, false)
    }

    this.recognition.onend = () => {
      console.log('🏁 识别结束，最终文本:', this.finalTranscript, '录音状态:', this.isRecording)
      // 触发最终结果
      const finalText = this.finalTranscript.trim()
      if (finalText) {
        onResult(finalText, true)
      }
      this.isRecording = false
    }

    this.recognition.onerror = (event: any) => {
      console.error('❌ 识别错误:', event.error)
      if (event.error !== 'no-speech' && event.error !== 'aborted') {
        onError('识别失败: ' + event.error)
      }
      this.isRecording = false
    }

    try {
      this.recognition.start()
      console.log('🎤 开始语音识别 (Web Speech API)')
    } catch (error: any) {
      onError('启动识别失败: ' + error.message)
      this.isRecording = false
    }
  }

  stopRecognition(): void {
    if (this.isRecording) {
      console.log('🛑 停止语音识别，当前文本:', this.finalTranscript)
      // 不要在这里设置isRecording = false，让onend事件处理
      this.recognition.stop()
    }
  }
}
