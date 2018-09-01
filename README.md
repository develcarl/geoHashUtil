# geoHashUtil
根据经纬度生成线性值，来判断当前位置附近的位置

# 使用方法
``` java
    private double lat = 39.9390715;
    private double lng = 116.1165893;
    
    public void testGetLocationBase32String(){
        System.out.println(GeoHashUtil.getLocationBase32String(lat, lng));
    }
    
    public void testGetLocationBase32StringAround(){
        System.out.println(GeoHashUtil.getLocationBase32StringAround(lat, lng));
    }
```
