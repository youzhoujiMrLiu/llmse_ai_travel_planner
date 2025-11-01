// src/services/amapService.ts
// 高德地图服务 - 地理编码和路线规划

declare global {
  interface Window {
    AMap: any
    _AMapSecurityConfig: any
  }
}

declare const AMap: any

export interface Location {
  name: string
  address?: string
  lng: number
  lat: number
}

export interface GeocodingResult {
  success: boolean
  location?: Location
  error?: string
}

/**
 * 高德地图服务类
 */
export class AmapService {
  private map: any = null
  private geocoder: any = null
  private placeSearch: any = null
  private markers: any[] = []
  private polylines: any[] = []

  /**
   * 初始化地图
   * @param containerId 地图容器 ID
   * @param center 中心点坐标 [lng, lat]
   * @param zoom 缩放级别
   */
  initMap(containerId: string, center: [number, number] = [116.397428, 39.90923], zoom: number = 10) {
    if (!window.AMap) {
      console.error('高德地图 SDK 未加载')
      return null
    }

    this.map = new AMap.Map(containerId, {
      zoom: zoom,
      center: center,
      viewMode: '3D', // 使用 3D 视图
      pitch: 50, // 俯仰角度
    })

    console.log('地图初始化成功')

    // 禁用地图容器的右键菜单
    const mapContainer = document.getElementById(containerId)
    if (mapContainer) {
      mapContainer.addEventListener('contextmenu', (e) => {
        e.preventDefault()
        return false
      })
      console.log('已禁用地图容器的右键菜单')
    }

    // 初始化地理编码器 - 确保 AMap.Geocoder 已加载
    if (window.AMap.Geocoder) {
      this.geocoder = new AMap.Geocoder({
        city: '全国', // 城市设为全国范围
      })
      console.log('地理编码器初始化成功')
    } else {
      console.error('AMap.Geocoder 插件未加载')
    }

    // 初始化地点搜索服务
    if (window.AMap.PlaceSearch) {
      this.placeSearch = new AMap.PlaceSearch({
        pageSize: 1, // 只取第一个结果
        pageIndex: 1,
        city: '全国',
        citylimit: false, // 不限制城市
        extensions: 'base' // 返回基本信息
      })
      console.log('地点搜索服务初始化成功')
    } else {
      console.error('AMap.PlaceSearch 插件未加载')
    }

    return this.map
  }

  /**
   * 验证地点是否在中国境内（通过高德 API 是否能返回结果判断）
   * @param destination 目的地名称
   * @returns Promise<{ isChina: boolean, error?: string }>
   */
  async validateChinaDestination(destination: string): Promise<{ isChina: boolean, error?: string }> {
    try {
      console.log(`🌍 验证目的地是否在中国: ${destination}`)
      
      const params = new URLSearchParams({
        address: destination
      })

      const response = await fetch(`http://localhost:8080/api/map/geocode?${params.toString()}`)
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const result = await response.json()
      
      console.log(`地理编码验证响应:`, result)

      // 高德 API 返回 status=1 且有 geocodes 数据,说明是中国境内地点
      if (result.status === '1' && result.geocodes && result.geocodes.length > 0) {
        console.log(`✅ "${destination}" 是中国境内地点`)
        return { isChina: true }
      } else {
        console.warn(`❌ "${destination}" 不是中国境内地点或无法定位`)
        return { 
          isChina: false, 
          error: result.info || '高德地图无法定位此地点'
        }
      }
    } catch (error: any) {
      console.error(`验证目的地异常: ${destination}`, error)
      // 网络错误等异常情况,保守处理,假定为中国境内(避免误判)
      return { 
        isChina: true, 
        error: '网络异常,无法验证目的地'
      }
    }
  }

  /**
   * 地点搜索 - 通过后端代理调用高德 Web 服务 API（推荐使用，比地理编码更准确）
   * @param keyword 地点名称关键词
   * @param city 限定城市（可选）
   * @returns Promise<GeocodingResult>
   */
  async searchPlace(keyword: string, city?: string): Promise<GeocodingResult> {
    try {
      console.log(`🔍 正在搜索地点: ${keyword}${city ? ` (城市: ${city})` : ''}`)

      // 通过后端接口调用高德 Web 服务 API
      const params = new URLSearchParams({
        keywords: keyword,
      })
      if (city) {
        params.append('city', city)
      }

      const response = await fetch(`http://localhost:8080/api/map/search?${params.toString()}`)
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const result = await response.json()

      console.log(`地点搜索响应:`, result)

      if (result.status === '1' && result.pois && result.pois.length > 0) {
        const poi = result.pois[0]
        const [lng, lat] = poi.location.split(',').map(Number)

        console.log(`✅ 地点搜索成功: ${keyword} -> ${poi.name} [${lng}, ${lat}]`)

        return {
          success: true,
          location: {
            name: poi.name,
            address: poi.address || (poi.pname + poi.cityname + poi.adname),
            lng: lng,
            lat: lat,
          },
        }
      } else {
        console.warn(`❌ 地点搜索失败: ${keyword}`)
        return {
          success: false,
          error: `未找到地点: ${keyword}`,
        }
      }
    } catch (error: any) {
      console.error(`地点搜索异常: ${keyword}`, error)
      return {
        success: false,
        error: `地点搜索异常: ${keyword}`,
      }
    }
  }

  /**
   * 地理编码 - 将地点名称/地址转换为坐标（已废弃，推荐使用 searchPlace）
   * @param address 地点名称或地址
   * @returns Promise<GeocodingResult>
   */
  async geocode(address: string): Promise<GeocodingResult> {
    return new Promise((resolve) => {
      if (!this.geocoder) {
        console.error('地理编码器未初始化，无法进行地理编码')
        resolve({ success: false, error: '地理编码器未初始化' })
        return
      }

      console.log(`正在对地点进行地理编码: ${address}`)

      // 设置超时，避免回调永远不返回
      const timeout = setTimeout(() => {
        console.error(`地理编码超时: ${address} (10秒无响应)`)
        resolve({
          success: false,
          error: `地理编码超时: ${address}`,
        })
      }, 10000)

      try {
        this.geocoder.getLocation(address, (status: string, result: any) => {
          clearTimeout(timeout)
          
          console.log(`地理编码回调触发 - 地点: ${address}, 状态: ${status}`, result)

          if (status === 'complete' && result.geocodes && result.geocodes.length > 0) {
            const geocode = result.geocodes[0]
            const location = geocode.location

            console.log(`✅ 地理编码成功: ${address} -> [${location.lng}, ${location.lat}]`)

            resolve({
              success: true,
              location: {
                name: address,
                address: geocode.formattedAddress,
                lng: location.lng,
                lat: location.lat,
              },
            })
          } else {
            console.warn(`❌ 地理编码失败: ${address}, 状态: ${status}, 结果:`, result)
            resolve({
              success: false,
              error: `无法定位: ${address} (状态: ${status})`,
            })
          }
        })
      } catch (error) {
        clearTimeout(timeout)
        console.error(`地理编码异常: ${address}`, error)
        resolve({
          success: false,
          error: `地理编码异常: ${address}`,
        })
      }
    })
  }

  /**
   * 批量地理编码
   * @param addresses 地点名称数组
   * @returns Promise<GeocodingResult[]>
   */
  async batchGeocode(addresses: string[]): Promise<GeocodingResult[]> {
    console.log(`开始批量地理编码，共 ${addresses.length} 个地点`)
    const results: GeocodingResult[] = []

    for (let i = 0; i < addresses.length; i++) {
      const address = addresses[i]
      if (!address) continue

      // 增加延迟到 500ms，避免高德 API 频率限制
      if (i > 0) {
        await new Promise((resolve) => setTimeout(resolve, 500))
      }
      const result = await this.geocode(address)
      results.push(result)
    }

    console.log(`批量地理编码完成，成功: ${results.filter(r => r.success).length}，失败: ${results.filter(r => !r.success).length}`)
    return results
  }

  /**
   * 在地图上添加标记点
   * @param location 位置信息
   * @param label 标签文字
   * @param icon 图标配置
   * @param options 可选配置 { onClick, onRightClick, customData }
   */
  addMarker(location: Location, label?: string, icon?: any, options?: {
    onClick?: (marker: any) => void
    onRightClick?: (marker: any) => void
    customData?: any
  }) {
    if (!this.map) {
      console.error('地图未初始化')
      return null
    }

    const marker = new AMap.Marker({
      position: [location.lng, location.lat],
      title: location.name,
      label: label
        ? {
            content: label,
            direction: 'top',
          }
        : undefined,
      icon: icon,
      extData: options?.customData // 存储自定义数据
    })

    marker.setMap(this.map)
    this.markers.push(marker)

    // 添加左键点击事件
    if (options?.onClick) {
      marker.on('click', (e: any) => {
        // 阻止事件冒泡,避免触发地图点击
        if (e.originEvent) {
          e.originEvent.stopPropagation()
        }
        options.onClick!(marker)
      })
    } else {
      // 默认显示信息窗体
      const infoWindow = new AMap.InfoWindow({
        content: `
          <div style="padding: 10px;">
            <h4 style="margin: 0 0 8px 0;">${location.name}</h4>
            <p style="margin: 0; color: #666; font-size: 12px;">${location.address || ''}</p>
          </div>
        `,
      })

      marker.on('click', () => {
        infoWindow.open(this.map, [location.lng, location.lat])
      })
    }

    // 添加右键点击事件
    if (options?.onRightClick) {
      marker.on('rightclick', (e: any) => {
        // 阻止浏览器默认右键菜单
        if (e.originEvent) {
          e.originEvent.preventDefault()
          e.originEvent.stopPropagation()
        }
        options.onRightClick!(marker)
      })
    }

    return marker
  }

  /**
   * 添加地图点击事件监听
   * @param callback 点击回调函数，参数为点击位置的经纬度和地址信息
   */
  onMapClick(callback: (location: { lng: number; lat: number; address?: string }) => void) {
    if (!this.map) {
      console.error('地图未初始化')
      return
    }

    this.map.on('click', async (e: any) => {
      const lngLat = e.lnglat
      console.log('地图点击位置:', lngLat.lng, lngLat.lat)
      
      // 逆地理编码获取地址
      try {
        const geocoder = new AMap.Geocoder({
          radius: 1000
        })
        
        geocoder.getAddress([lngLat.lng, lngLat.lat], (status: string, result: any) => {
          if (status === 'complete' && result.info === 'OK') {
            const address = result.regeocode.formattedAddress
            callback({
              lng: lngLat.lng,
              lat: lngLat.lat,
              address: address
            })
          } else {
            callback({
              lng: lngLat.lng,
              lat: lngLat.lat
            })
          }
        })
      } catch (error) {
        console.error('逆地理编码失败:', error)
        callback({
          lng: lngLat.lng,
          lat: lngLat.lat
        })
      }
    })
  }

  /**
   * 移除地图点击事件监听
   */
  offMapClick() {
    if (!this.map) return
    this.map.off('click')
  }

  /**
   * 绘制路径(折线)
   * @param locations 位置数组
   * @param color 线条颜色
   * @param showDirection 是否显示方向箭头
   */
  drawPath(locations: Location[], color: string = '#3b82f6', showDirection: boolean = true) {
    if (!this.map || locations.length < 2) {
      return null
    }

    const path = locations.map((loc) => [loc.lng, loc.lat])

    const polyline = new AMap.Polyline({
      path: path,
      strokeColor: color,
      strokeWeight: 6,
      strokeOpacity: 0.8,
      lineJoin: 'round',
      lineCap: 'round',
      showDir: showDirection, // 显示方向箭头
    })

    polyline.setMap(this.map)
    this.polylines.push(polyline)

    return polyline
  }

  /**
   * 使用驾车路径规划绘制路线
   * @param startLocation 起点
   * @param endLocation 终点
   * @param waypoints 途经点
   */
  async drawDrivingRoute(
    startLocation: Location,
    endLocation: Location,
    waypoints?: Location[]
  ) {
    return new Promise((resolve, reject) => {
      if (!this.map) {
        reject('地图未初始化')
        return
      }

      const driving = new AMap.Driving({
        map: this.map,
        policy: AMap.DrivingPolicy.LEAST_TIME, // 最快路线
      })

      const waypointPositions = waypoints?.map((wp) => [wp.lng, wp.lat]) || []

      driving.search(
        [startLocation.lng, startLocation.lat],
        [endLocation.lng, endLocation.lat],
        { waypoints: waypointPositions },
        (status: string, result: any) => {
          if (status === 'complete') {
            console.log('路线规划成功', result)
            resolve(result)
          } else {
            console.error('路线规划失败', result)
            reject(result)
          }
        }
      )
    })
  }

  /**
   * 清除所有标记点
   */
  clearMarkers() {
    this.markers.forEach((marker) => marker.setMap(null))
    this.markers = []
  }

  /**
   * 清除所有路线
   */
  clearPolylines() {
    this.polylines.forEach((polyline) => polyline.setMap(null))
    this.polylines = []
  }

  /**
   * 清除所有覆盖物(标记点和路线)
   */
  clearAll() {
    this.clearMarkers()
    this.clearPolylines()
    if (this.map) {
      this.map.clearMap()
    }
  }

  /**
   * 自动调整地图视野以适应所有标记点
   */
  fitView() {
    if (this.map && this.markers.length > 0) {
      this.map.setFitView(this.markers)
    }
  }

  /**
   * 禁用地图交互(拖拽、缩放等)
   */
  disableMapInteraction() {
    if (!this.map) return
    
    // 禁用地图拖拽
    this.map.setStatus({
      dragEnable: false,
      zoomEnable: false,
      doubleClickZoom: false,
      scrollWheel: false,
      touchZoom: false,
      touchZoomCenter: false
    })
    
    console.log('地图交互已禁用')
  }

  /**
   * 启用地图交互(拖拽、缩放等)
   */
  enableMapInteraction() {
    if (!this.map) return
    
    // 启用地图拖拽
    this.map.setStatus({
      dragEnable: true,
      zoomEnable: true,
      doubleClickZoom: true,
      scrollWheel: true,
      touchZoom: true,
      touchZoomCenter: true
    })
    
    console.log('地图交互已启用')
  }

  /**
   * 销毁地图
   */
  destroy() {
    if (this.map) {
      this.map.destroy()
      this.map = null
    }
    this.geocoder = null
    this.markers = []
    this.polylines = []
  }
}

/**
 * 全局单例
 */
let amapServiceInstance: AmapService | null = null

export function getAmapService(): AmapService {
  if (!amapServiceInstance) {
    amapServiceInstance = new AmapService()
  }
  return amapServiceInstance
}
