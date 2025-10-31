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

    // 初始化地理编码器
    this.geocoder = new AMap.Geocoder({
      city: '全国', // 城市设为全国范围
    })

    return this.map
  }

  /**
   * 地理编码 - 将地点名称/地址转换为坐标
   * @param address 地点名称或地址
   * @returns Promise<GeocodingResult>
   */
  async geocode(address: string): Promise<GeocodingResult> {
    return new Promise((resolve) => {
      if (!this.geocoder) {
        resolve({ success: false, error: '地理编码器未初始化' })
        return
      }

      this.geocoder.getLocation(address, (status: string, result: any) => {
        if (status === 'complete' && result.geocodes && result.geocodes.length > 0) {
          const geocode = result.geocodes[0]
          const location = geocode.location

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
          resolve({
            success: false,
            error: `无法定位: ${address}`,
          })
        }
      })
    })
  }

  /**
   * 批量地理编码
   * @param addresses 地点名称数组
   * @returns Promise<GeocodingResult[]>
   */
  async batchGeocode(addresses: string[]): Promise<GeocodingResult[]> {
    const results: GeocodingResult[] = []

    for (const address of addresses) {
      // 添加短暂延迟避免请求过快
      await new Promise((resolve) => setTimeout(resolve, 200))
      const result = await this.geocode(address)
      results.push(result)
    }

    return results
  }

  /**
   * 在地图上添加标记点
   * @param location 位置信息
   * @param label 标签文字
   * @param icon 图标配置
   */
  addMarker(location: Location, label?: string, icon?: any) {
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
    })

    marker.setMap(this.map)
    this.markers.push(marker)

    // 添加点击事件显示信息窗体
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

    return marker
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
