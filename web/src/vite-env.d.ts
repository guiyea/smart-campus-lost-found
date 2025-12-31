/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// 高德地图 AMap 类型声明
declare const AMap: {
  Map: new (container: HTMLElement | string, options?: any) => any
  Marker: new (options?: any) => any
  Geocoder: new (options?: any) => any
  PlaceSearch: new (options?: any) => any
  Geolocation: new (options?: any) => any
  InfoWindow: new (options?: any) => any
  Icon: new (options?: any) => any
  Size: new (width: number, height: number) => any
  Pixel: new (x: number, y: number) => any
  Scale: new (options?: any) => any
  ToolBar: new (options?: any) => any
  plugin: (plugins: string | string[], callback: () => void) => void
}




