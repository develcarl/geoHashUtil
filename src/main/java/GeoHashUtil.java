import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yichen
 * @description 计算geohash工具类
 * @date 2018/4/8
 **/
public class GeoHashUtil {

    /**
     * 最小纬度
     */
    private static final double MIN_LAT = -90;

    /**
     * 最大纬度
     */
    private static final double MAX_LAT = 90;

    /**
     * 最大经度
     */
    private static final double MAX_LNG = 180;

    /**
     * 最小经度
     */
    private static final double MIN_LNG = -180;

    /**
     * base 32 对应binary长度
     */
    private static final int BASE_32_BITS = 5;

    /**
     * geohash最后长度
     * average error
     *
     * 1   2500   km
     * 2   630    km
     * 3   78     km
     * 4   20     km
     * 5   2.4    km
     * 6   610    m
     * 7   76     m
     * 8   19     m
     */
    public static int HASH_LENGTH = 8;

    /**
     * 纬度binary长度
     */
    private static int LAT_LENGTH = 20;

    /**
     * 经度binary长度
     */
    private static int LNG_LENGTH = 20;

    /**
     * 依据纬度binary长度算出的最小纬度半值
     */
    private static double minLat;

    /**
     * 依据经度binary长度算出的最小经度半值
     */
    private static double minLng;

    /**
     * runtime时计算经纬度最小半值
     */
    static {
        setMinLatAndLng();
    }

    private static final char[] BASE_32_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n',
            'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    /**
     * 不同精度的误差值
     * 1   2500   km
     * 2   630    km
     * 3   78     km
     * 4   20     km
     * 5   2.4    km
     * 6   610    m
     * 7   76     m
     * 8   19     m
     */
    public static final double[] GEO_AVERAGE_ERROR = {2500, 630, 78, 20, 2.4, 0.61, 0.076, 0.019};

    public int getHashLength(){
        return HASH_LENGTH;
    }

    /**
     * 获得当前位置的base32编码
     * @param lat
     * @param lng
     * @return
     */
    public static String getLocationBase32String(double lat, double lng){

        int[] latHashArray = getBinaryArray(lat, MAX_LAT, MIN_LAT, LAT_LENGTH);
        int[] lngHashArray = getBinaryArray(lng, MAX_LNG, MIN_LNG, LNG_LENGTH);

        int[] mergeHashArray = mergeHashArray(latHashArray, lngHashArray);

        StringBuilder base32StringBuilder = new StringBuilder();
        int[] binaryArray = new int[BASE_32_BITS];
        for (int i = 0; i < mergeHashArray.length; i++){
            binaryArray[i % BASE_32_BITS] = mergeHashArray[i];
            if ((i + 1) % BASE_32_BITS == 0){
                Integer index = binary2Decimal(binaryArray);
                base32StringBuilder.append(getBase32Char(index));
            }
        }

        return base32StringBuilder.toString();

    }

    /**
     * 获得当前位置周围八个等大区域的base32编码
     * @param lat
     * @param lng
     * @return
     */
    public static List<String> getLocationBase32StringAround(double lat, double lng){
        double leftLat = lat - minLat;
        double rightLat = lat + minLat;
        double upLng = lng - minLng;
        double downLng = lng + minLng;
        List<String> base32Around = new ArrayList<String>();

        String currLocationHash = getLocationBase32String(lat, lng);
        if (StringUtils.isNotBlank(currLocationHash)){
            base32Around.add(currLocationHash);
        }

        //左上
        String leftUp = getLocationBase32String(leftLat, upLng);
        if (StringUtils.isNotBlank(leftUp)){
            base32Around.add(leftUp);
        }
        //左中
        String leftMid = getLocationBase32String(leftLat, lng);
        if (StringUtils.isNotBlank(leftMid)){
            base32Around.add(leftMid);
        }
        //左下
        String leftDown = getLocationBase32String(leftLat, downLng);
        if (StringUtils.isNotBlank(leftDown)){
            base32Around.add(leftDown);
        }
        //中下
        String midDown = getLocationBase32String(lat, downLng);
        if (StringUtils.isNotBlank(midDown)){
            base32Around.add(midDown);
        }
        //右下
        String rightDown = getLocationBase32String(rightLat, downLng);
        if (StringUtils.isNotBlank(rightDown)){
            base32Around.add(rightDown);
        }
        //右中
        String rightMid = getLocationBase32String(rightLat, lng);
        if (StringUtils.isNotBlank(rightMid)){
            base32Around.add(rightMid);
        }
        //右上
        String rightUp = getLocationBase32String(rightLat, upLng);
        if (StringUtils.isNotBlank(rightUp)){
            base32Around.add(rightUp);
        }
        //中上
        String midUp = getLocationBase32String(lat, upLng);
        if (StringUtils.isNotBlank(midUp)){
            base32Around.add(midUp);
        }

        return base32Around;
    }

    /**
     * 二进制转十进制
     * @param binary
     * @return
     */
    private static Integer binary2Decimal(int[] binary){
        int decimal = 0;
        for (int i = 0; i < binary.length; i++){
            if (binary[i] != 0){
                decimal += (binary[i] << (BASE_32_BITS - i - 1));
            }
        }
        return decimal;
    }

    /**
     * 获取对应index的base32值
     * @param index
     * @return
     */
    private static char getBase32Char(int index){
        return BASE_32_DIGITS[index];
    }

    /**
     * 合并两个boolean数组，纬度(lat)占奇数位，经度(lng)占偶数位
     * @param latResult
     * @param lngResult
     * @return
     */
    private static int[] mergeHashArray(int latResult[], int lngResult[]){
        int[] mergeResult = new int[LAT_LENGTH + LNG_LENGTH];

        for (int i = 0; i < LNG_LENGTH; i++){
            mergeResult[i * 2] = lngResult[i];
        }

        for (int i = 0; i < LAT_LENGTH; i++){
            mergeResult[i * 2 + 1] = latResult[i];
        }

        return mergeResult;

    }

    /**
     * 计算当前位置经度或纬度的二进制值，逻辑为折半判断，大于等于中间值得 1 反之则为 0
     * @param value 经度或纬度
     * @param max 经度或纬度的最大值
     * @param min 经度或纬度的最小值
     * @param length 经度或纬度的最大长度
     * @return
     */
    private static int[] getBinaryArray(double value, double max, double min, int length){
        if (value > max || value < min || length < 1){
            return null;
        }

        int[] result = new int[length];

        for (int i = 0; i < length; i++){
            double mid = (max + min) / 2;
            if (value >= mid){
                result[i] = 1;
                min = mid;
            }else {
                result[i] = 0;
                max = mid;
            }
        }

        return result;
    }

    /**
     * 设置最后位置base32值的长度
     * @param length
     * @return
     */
    public static boolean setHashLength(int length){
        if (length < 1){
            return false;
        }

        HASH_LENGTH = length;
        LAT_LENGTH = length * BASE_32_BITS / 2;

        if (length % 2 == 0){
            LNG_LENGTH = LAT_LENGTH;
        }else {
            LNG_LENGTH = LAT_LENGTH + 1;
        }

        setMinLatAndLng();

        return true;
    }

    /**
     * 根据当前经纬度二进制值长度计算最小经纬度半值
     */
    private static void setMinLatAndLng(){
        minLat = MAX_LAT - MIN_LAT;
        for (int i = 0; i < LAT_LENGTH; i++){
            minLat /= 2.0;
        }

        minLng = MAX_LNG - MIN_LNG;
        for (int i = 0; i < LNG_LENGTH; i++){
            minLng /= 2.0;
        }
    }


}
