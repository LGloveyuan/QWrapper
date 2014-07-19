import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;




import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.OneWayFlightInfo;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.bean.search.RoundTripFlightInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;

/**
 * 
 * @ClassName: Wrapper_gjdairju001 
 * @Description:  科西嘉国际航空
 * @author: LG
 * @date 2014-7-10 
 *
 */
public class Wrapper_gjsairss001 implements QunarCrawler {
	
	
	public static final int POST = 0;
	
	public static final int GET = 1;
	
	public static final String HOME_URL = "http://www.corsair.ca/flight/home";
	
	public static String SEARCH_FLIGHT_URL = "https://www.corsair.ca/flight/content/action" ;
	
	public static void main(String[] args) {

		FlightSearchParam searchParam = new FlightSearchParam();
		searchParam.setDep("SFG");
		searchParam.setArr("FDF");
		
		searchParam.setDepDate("2014-09-18");
		searchParam.setRetDate("2014-09-25");
		
		/*searchParam.setDep("DKR");
		searchParam.setArr("ORY");
		
		searchParam.setDepDate("2014-07-20");
		searchParam.setRetDate("2014-07-29");*/
		
		//无效日期
		/*searchParam.setDep("DZA");
		searchParam.setArr("ORY");
		searchParam.setDepDate("2014-09-14");
		searchParam.setRetDate("2014-09-14");*/
		searchParam.setTimeOut("60000");
		searchParam.setToken("");
		searchParam.setWrapperid("gjsairen001");
		

		String html = new Wrapper_gjsairss001().getHtml(searchParam);
		System.out.println("HTML: " + html);
		
		ProcessResultInfo result = new ProcessResultInfo();

		result = new Wrapper_gjsairss001().process(html, searchParam);
		
		if (result.isRet() && result.getStatus().equals(Constants.SUCCESS)) {
			List<RoundTripFlightInfo> flightList = (List<RoundTripFlightInfo>) result
					.getData();
			for (RoundTripFlightInfo in : flightList) {
				System.out.println("************" + in.getInfo().toString());
				System.out.println("++++++++++++" + in.getDetail().toString());

			}
			
			
			String jsonString = JSON.toJSONString(flightList);
			System.out.println(jsonString);
			
		} else {
			System.out.println(result.getStatus());
		}
		
	}

	@Override
	public BookingResult getBookingInfo(FlightSearchParam arg0) {

		    BookingResult bookingResult = new BookingResult();
		    
		    BookingInfo bookingInfo = new BookingInfo();
		    bookingInfo.setAction(SEARCH_FLIGHT_URL);
		    bookingInfo.setInputs(getSearchParamMapForSingle(arg0));
		    bookingInfo.setMethod("post");
		    
		    bookingResult.setData(bookingInfo);
		    bookingResult.setRet(true);
		    
		    return bookingResult;		    
		}

	/**
	 * 鑾峰彇鏈夋晥鑸彮缁撴灉淇℃伅
	 */
	public String getHtml(FlightSearchParam searchParam) {
		Protocol myhttps = new Protocol("https", new MySSLSocketFactory(), 443);  
   	    Protocol.registerProtocol("https", myhttps);  
		try {
			String html = getResultHtml(getHttp(searchParam), POST,SEARCH_FLIGHT_URL,getSearchParamMapForSingle(searchParam));
			
			String resultJson = org.apache.commons.lang.StringUtils.substringBetween(html, "generatedJSon = new String('", "');");
			System.out.println(resultJson);
			
		    return resultJson;
			
			
		} catch (Exception e) {
		}
			
		return "Exception";
		
	}

	/**
	 * 鑾峰彇缁撴灉椤甸潰HTML
	 * @param client
	 * @param method
	 * @param uri
	 * @param paramMap
	 * @return
	 */
    private String getResultHtml(HttpClient client ,int method,String uri,Map<String,String> paramMap) {
    	 Protocol myhttps = new Protocol("https", new MySSLSocketFactory(), 443);  
    	 Protocol.registerProtocol("https", myhttps);  

    	HttpMethod httpMethod = null ;

		try {
			httpMethod = initMethod(method,uri,paramMap) ;
			
			int statusCode = client.executeMethod(httpMethod);  
			System.out.println("statusCode: " + statusCode);
			if(statusCode >=300 && statusCode <=399){
				Header location = httpMethod.getResponseHeader("Location");
				String url = "";
				if(location !=null){
					url = location.getValue();
					if(!url.startsWith("http")){
						url = httpMethod.getURI().getScheme() + "://" + httpMethod.getURI().getHost() + (httpMethod.getURI().getPort()==-1?"":(":"+httpMethod.getURI().getPort())) + url;
						return this.requestGet(client,url) ;
					}
				}
			}
			
			return httpMethod.getResponseBodyAsString() ;
			
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(null != httpMethod){
				httpMethod.releaseConnection();
			}
		}
		return null ;
		
	}
    

    private String requestGet(HttpClient client ,String uri) {
		return request(client,GET, uri);
	}
    
    
	private String request(HttpClient client ,int method, String uri) {
		return getResultHtml(client,method, uri, null);
	}
	
	/**
	 * 鍒濆鍖朠OST鎴栬�GET鏂规硶
	 * @param method
	 * @param uri
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
    private HttpMethodBase initMethod(int method, String uri,
			Map<String, String> paramMap) throws Exception {

		if (method == POST) {
			PostMethod post = new QFPostMethod(uri);
			if (null != paramMap) {
				for (Map.Entry<String, String> param : paramMap.entrySet()) {
					post.addParameter(param.getKey(), param.getValue());
				}
			
			}
			return post;
		} else if (method == GET) {
			StringBuilder result = new StringBuilder();
			
			for (Map.Entry<String, String> param : paramMap.entrySet()) {
				if (result.length() > 0)
	                  result.append("&");
	              result.append(param.getKey());
	              result.append("=");
	              result.append(param.getValue());
			}
			String allUrl = uri.indexOf("?") > 0 ? (uri + "&" + result) : (uri + "?" + result);
			System.out.println("allURL: " + allUrl);
	
			GetMethod get = new QFGetMethod(allUrl);
		
			return get;
		} else {
			throw new Exception("Http method invalid!");
		}
	}

    /**
     * 鑾峰彇鍙傛暟瀵�
     * @param searchParam
     * @return
     */
    private static Map<String,String> getSearchParamMapForSingle(FlightSearchParam searchParam){
		Map<String,String> param = new HashMap<String,String>();
		//鍑哄彂鏃ユ湡澶勭悊
		String[] depDate = getYearMonthDay(searchParam.getDepDate());
		//杩旂▼鏃ユ湡澶勭悊
		String[] retDate = getYearMonthDay(searchParam.getRetDate());
		param.put("TopLevelNode","1043");
		param.put("ContentNodeID","1043");
		param.put("ContentObjectID","1159");
		param.put("language_code","eng-CA");
		param.put("siteaccess","engca");
		param.put("CallActionHandler","");
	//	param.put("LiaisonID","819");   //鍊兼槸鍙樺寲鐨�
	//	param.put("LiaisonID2","2842");  //鍊兼槸鍙樺寲鐨�
		
		param.put("LiaisonID","");   //鍊兼槸鍙樺寲鐨�
		param.put("LiaisonID2","");  //鍊兼槸鍙樺寲鐨�
		
		param.put("ContentObjectAttribute_ezstring_data_text_258665",searchParam.getDep());
		param.put("ContentObjectAttribute_ezstring_data_text_258666",searchParam.getArr());
		param.put("ContentObjectAttribute_ezstring_data_text_258674",searchParam.getArr());
		param.put("ContentObjectAttribute_ezstring_data_text_258675",searchParam.getDep());
		
		param.put("ContentObjectAttribute_ezselect_selected_array_258667[]","0");
	//	param.put("departuredate","07/15/2014");
		param.put("departuredate", depDate[1]+"/"+depDate[2]+"/"+depDate[0]);
		param.put("ContentObjectAttribute_datetime_year_258663",depDate[0]);
		param.put("ContentObjectAttribute_datetime_month_258663",depDate[1]);
		param.put("ContentObjectAttribute_datetime_day_258663",depDate[2]);
		param.put("ContentObjectAttribute_datetime_hour_258663","13");
		param.put("ContentObjectAttribute_datetime_minute_258663","12");
		
		param.put("ContentObjectAttribute_datetime_year_258664",retDate[0]);
		param.put("ContentObjectAttribute_datetime_month_258664",retDate[1]);
		param.put("ContentObjectAttribute_datetime_day_258664",retDate[2]);
		param.put("ContentObjectAttribute_datetime_hour_258664","13");
		param.put("ContentObjectAttribute_datetime_minute_258664","12");
		
		param.put("ContentObjectAttribute_ezselect_selected_array_258676[]","1");
		param.put("ContentObjectAttribute_ezselect_selected_array_258668","");
		param.put("ContentObjectAttribute_ezselect_selected_array_258668[]","0");
		param.put("ContentObjectAttribute_ezselect_selected_array_258669","");
		param.put("ContentObjectAttribute_ezselect_selected_array_258669[]","1");
		param.put("ContentObjectAttribute_ezselect_selected_array_258670","");
		param.put("ContentObjectAttribute_ezselect_selected_array_258670[]","0");
		param.put("ContentObjectAttribute_ezselect_selected_array_2149562","");
		param.put("ContentObjectAttribute_ezselect_selected_array_2149562[]","0");
		
		param.put("ContentObjectAttribute_ezselect_selected_array_588921","");
		param.put("ContentObjectAttribute_ezselect_selected_array_588921[]","0");
		param.put("ContentObjectAttribute_ezselect_selected_array_258671","");
		param.put("ContentObjectAttribute_ezselect_selected_array_258671[]","0");
		param.put("ContentObjectAttribute_ezselect_selected_array_258673","");
		param.put("ContentObjectAttribute_ezselect_selected_array_258673[]","0");
		param.put("ContentObjectAttribute_ezselect_selected_array_2149689","");
		param.put("ContentObjectAttribute_ezselect_selected_array_2149689[]","0");
		
		return param ;
		
	}
	
    public ProcessResultInfo process(String arg0, FlightSearchParam arg1) {
		    String html = arg0;
	        System.out.println(html);
			
			/* ProcessResultInfo涓紝
			 * ret涓簍rue鏃讹紝status鍙互涓猴細SUCCESS(鎶撳彇鍒版満绁ㄤ环鏍�|NO_RESULT(鏃犵粨鏋滐紝娌℃湁鍙崠鐨勬満绁�
			 * ret涓篺alse鏃讹紝status鍙互涓�CONNECTION_FAIL|INVALID_DATE|INVALID_AIRLINE|PARSING_FAIL|PARAM_ERROR
			 */
			ProcessResultInfo result = new ProcessResultInfo();
			if ("Exception".equals(html)) {	
				result.setRet(false);
				result.setStatus(Constants.CONNECTION_FAIL);
				return result;			
			}		
			//闇�鏈夋槑鏄剧殑鎻愮ず璇彞锛屾墠鑳藉垽鏂槸鍚NVALID_DATE|INVALID_AIRLINE|NO_RESULT
			if (null != html && html.indexOf("siteParameters") != -1) {
				result.setRet(false);
				result.setStatus(Constants.INVALID_DATE);
				return result;			
			}
			
			// 定义价格和航班映射Map**************************************************************
			Map<String, Double> depPrices = new HashMap<String, Double>();
			Map<String, Double> retPrices = new HashMap<String, Double>();
			//*******************************************************************************

		//	String listSegment = org.apache.commons.lang.StringUtils.substringBetween(html, "\"list_segment\":", ",\"flight_id\"");
			String listFlight = org.apache.commons.lang.StringUtils.substringBetween(html, "\"list_proposed_bound\":", ",\"list_recommendation\"");
			System.out.println(listFlight);
			String listDetail = org.apache.commons.lang.StringUtils.substringBetween(html, "\"list_recommendation\":", ",\"list_date\":");		
			System.out.println(listDetail);
			try {			
				// 生成价格和航班的映射*************************
				getPriceTax(listDetail, depPrices, 0);
				getPriceTax(listDetail, retPrices, 1);
				//****************************************
				List<RoundTripFlightInfo> roundList = new ArrayList<RoundTripFlightInfo>() ;
				JSONArray flightJson = JSON.parseArray(listFlight);	
				JSONObject oneJson = flightJson.getJSONObject(0);
				JSONObject retJson = flightJson.getJSONObject(1);
				JSONArray oneFlightJSON = JSON.parseArray(oneJson.getString("list_flight"));
				JSONArray retFlightJSON = JSON.parseArray(retJson.getString("list_flight"));
				
				for(int i = 0; i < oneFlightJSON.size(); i++){
					//鍘荤▼鑸
					JSONObject ojson = oneFlightJSON.getJSONObject(i);
					JSONArray segJSON = JSON.parseArray(ojson.getString("list_segment"));
					//获取价格和税*******************************************
					String flight_id = ojson.getString("flight_id");
					double price = 0;
					double tax = 0;
					price = depPrices.get(flight_id + "price");
					tax = depPrices.get(flight_id + "tax");
					
					JSONArray djson = JSON.parseArray(listDetail);
					
					for(int j = 0; j < retFlightJSON.size(); j++){
						
						//*****************************************************
						List<FlightSegement> oneSegs = getFlightSegements(segJSON);
						List<String> flightNos = getFlightNoList(segJSON);
						FlightDetail flightDetail = new FlightDetail();
						flightDetail.setFlightno(flightNos);
						flightDetail.setDepdate(getDepdate(segJSON, true));
						String[] arr = getDepAndArrCity(segJSON, true);
						
						flightDetail.setDepcity(arr[0]);
						flightDetail.setArrcity(arr[1]);
						
						flightDetail.setMonetaryunit("CAD");
						flightDetail.setWrapperid(arg1.getWrapperid());
						
						
						
						RoundTripFlightInfo  retFlight = new RoundTripFlightInfo();
						
					     //杩斿洖鑸
						JSONObject rjson = retFlightJSON.getJSONObject(j);
						JSONArray retsegJSON = JSON.parseArray(rjson.getString("list_segment"));
						//***********************************************************************
						String ret_flight_id = rjson.getString("flight_id");
						double ret_price = 0;
						double ret_tax = 0;
						ret_price = retPrices.get(ret_flight_id + "price");
						ret_tax = retPrices.get(ret_flight_id + "tax");
						//************************************************************************
						List<FlightSegement> retSegs = getFlightSegements(retsegJSON);
						List<String> retFlightNos = getFlightNoList(retsegJSON);
						
						JSONArray jsonArray = JSON.parseArray(djson.getJSONObject(0).getString("list_trip_price"));
					    
					    JSONObject priceJson = jsonArray.getJSONObject(0);
					    JSONArray array = JSON.parseArray(priceJson.getString("list_bound_price"));
					    JSONObject object = array.getJSONObject(0);
					    JSONObject retobject = array.getJSONObject(1);
					    
					  //*************************************************
						flightDetail.setPrice(price+ret_price+tax+ret_tax);
						flightDetail.setTax(tax+ret_tax);
						//**************************************************
					    
					//	flightDetail = getFlightDetail(flightDetail,jsonArray,ojson,arg1);
						//*********************************
					//	flightDetail.setPrice(ret_price);
					//	flightDetail.setTax(ret_tax);
						//**********************************
						retFlight.setInfo(oneSegs);
						retFlight.setRetinfo(retSegs);
						retFlight.setDetail(flightDetail) ;//detail
				//		retFlight.setOutboundPrice(object.getDouble("total_amount")) ;//去程价格
						retFlight.setOutboundPrice(price+tax) ;//去程价格
						
					//	retFlight.setRetdepdate(getDepdate(retsegJSON, false)) ;//返程日期
						retFlight.setRetdepdate(getDate(arg1.getRetDate())) ;//返程日期
						retFlight.setRetflightno(retFlightNos) ; //返程日期
						
				//		retFlight.setReturnedPrice(retobject.getDouble("total_amount")) ;//返程价格
						retFlight.setReturnedPrice(ret_price+ret_tax) ;//返程价格
						roundList.add(retFlight);
						
					}
					
				}
				
				//娴嬭瘯
				String jsonString = JSON.toJSONString(roundList);
				System.out.println(jsonString);
				
				if(roundList.size()==0)
				{
					result.setRet(false);
					result.setStatus(Constants.PARSING_FAIL);
					return result;
				}
				
				result.setRet(true);
				result.setStatus(Constants.SUCCESS);
				result.setData(roundList);
				return result;
			} catch(Exception e){
				result.setRet(false);
				result.setStatus(Constants.PARSING_FAIL);
				return result;
			}
		}
	
	private void getPriceTax(String listDetail, Map<String, Double> prices, int flag) 
	{
		JSONArray recommendation = JSON.parseArray(listDetail);
		
		for(int i=0;i<recommendation.size();i++)
		{
			JSONObject ojson1 = recommendation.getJSONObject(i);
			System.out.println("ojson1" + i + ": " + ojson1);
			
			JSONArray bound = ojson1.getJSONArray("list_bound");
			System.out.println("bound: " + bound);
			JSONArray trip_price = ojson1.getJSONArray("list_trip_price");
			System.out.println("trip_price: " + trip_price);
			JSONObject ojson2 = trip_price.getJSONObject(0).getJSONArray("list_bound_price").getJSONObject(flag);
			
			String _price = ojson2.getString("amount_without_tax");
			System.out.println("price: " + _price);
			double price = Double.MAX_VALUE;
			if(null != _price) price = Double.parseDouble(_price); 
			String _tax = ojson2.getString("tax");
			System.out.println("tax: " + _tax);
			double tax = 0;
			if(null != _tax) tax = Double.parseDouble(_tax);
			
			JSONObject ojson3 = bound.getJSONObject(flag);
			JSONArray flights = ojson3.getJSONArray("list_flight");
			System.out.println("flights: " + flights);
			for(int j=0;j<flights.size();j++)
			{
				JSONObject ojson4 = flights.getJSONObject(j);
				String flight_id = ojson4.getString("flight_id");
				System.out.println("flight_id: " + flight_id);
				Double temp_price = prices.get(flight_id + "price");
				Double temp_tax = prices.get(flight_id + "tax");
				System.out.println("temp_price: " + temp_price);
				if(null == temp_price || (price+tax) < (temp_price+temp_tax))
				{
					prices.put(flight_id + "price", price);
					prices.put(flight_id + "tax", tax);
				}
			}
		}
		
	}

	/**
	 * 
	 * @Title: getFlightSegements 
	 * @Description: 鑾峰彇鑸彮淇℃伅
	 * @param segJSON
	 * @return聽聽聽 聽
	 * @throws
	 */
	private List<FlightSegement> getFlightSegements(JSONArray segJSON){
		List<FlightSegement> oneSegs = new ArrayList<FlightSegement>();
		List<String> flightNoList = new ArrayList<String>();
		
		for(int i = 0; i < segJSON.size(); i++){
			FlightSegement seg = new FlightSegement();
			JSONObject fjson = segJSON.getJSONObject(i);
			JSONObject preFlightNo = JSON.parseObject(fjson.getString("airline"));
			String pre = preFlightNo.getString("code");
			String flightNo = fjson.getString("flight_number").replaceAll("[^a-zA-Z\\d]", "");
			String depString = fjson.getString("b_date_date");
			String arrString = fjson.getString("e_date_date");
			String formatDep = "";
			if(null != depString && !"".equals(depString)){
				formatDep = depString.substring(0, 4)+"-"+depString.substring(4,6)+"-"+depString.substring(6,8);
			}
			
			String formatArr = "";
			if(null != arrString && !"".equals(arrString)){
				formatArr = arrString.substring(0, 4)+"-"+arrString.substring(4,6)+"-"+arrString.substring(6,8);
			}
			JSONObject beginLocation = JSON.parseObject(fjson.getString("b_location"));
			String depairport = beginLocation.getString("location_code").replaceAll("[^a-zA-Z\\d]", "");
			JSONObject endLocation = JSON.parseObject(fjson.getString("e_location"));
			String arrairport = endLocation.getString("location_code").replaceAll("[^a-zA-Z\\d]", "");
			flightNoList.add(pre+flightNo);
			seg.setFlightno(pre+flightNo);
			seg.setDepDate(formatDep);
			seg.setArrDate(formatArr);
			seg.setDepairport(depairport);
			seg.setArrairport(arrairport);
			seg.setDeptime(fjson.getString("b_date_formatted_time"));
			seg.setArrtime(fjson.getString("e_date_formatted_time"));
			
			oneSegs.add(seg);
		}
		return oneSegs;
	}
	
	/**
	 * 
	 * @Title: getFlightNoList 
	 * @Description: 鑾峰彇鑸彮鍙峰垪琛�
	 * @param segJSON
	 * @return聽聽聽 聽
	 * @throws
	 */
	private List<String> getFlightNoList(JSONArray segJSON){
		List<String> flightNoList = new ArrayList<String>();

		for(int i = 0; i < segJSON.size(); i++){
			JSONObject fjson = segJSON.getJSONObject(i);
			JSONObject preFlightNo = JSON.parseObject(fjson.getString("airline"));
			String pre = preFlightNo.getString("code");
			String flightNo = fjson.getString("flight_number").replaceAll("[^a-zA-Z\\d]", "");
			flightNoList.add(pre+flightNo);
		}
		return flightNoList;
	}
	
	/**
	 * 
	 * @Title: getDepdate 
	 * @Description: 鑾峰彇鍑哄彂鏃ユ湡鍜岃繑绋嬫棩鏈�
	 * @param segJSON
	 * @param flag
	 * @return聽聽聽 聽
	 * @throws
	 */
	private Date getDepdate(JSONArray segJSON,boolean flag){
		Date depDate = new Date();
		for(int i = 0; i < segJSON.size(); i++){
			JSONObject fjson = segJSON.getJSONObject(i);
			if(flag == true){
				depDate = fjson.getDate("b_date_date");
			}else {
				depDate = fjson.getDate("e_date_date");
			}
			
		}
		return depDate;
	}
	
	/**
	 * 鑾峰彇鍩庡競涓夊瓧鐮�
	 * @param segJSON
	 * @param flag
	 * @return
	 */
	private String[] getDepAndArrCity(JSONArray segJSON,boolean flag){
		String[] arr = new String[2];
        if(segJSON.size() == 1){
            JSONObject fjson = segJSON.getJSONObject(0);
			
			JSONObject beginLocation = JSON.parseObject(fjson.getString("b_location"));
			String depCity = beginLocation.getString("city_code").replaceAll("[^a-zA-Z\\d]", "");
			JSONObject endLocation = JSON.parseObject(fjson.getString("e_location"));
			String arrCity = endLocation.getString("city_code").replaceAll("[^a-zA-Z\\d]", "");
			arr[0] = depCity;
			arr[1] = arrCity;
		}else{
			 JSONObject fjson1 = segJSON.getJSONObject(0);
			 JSONObject beginLocation = JSON.parseObject(fjson1.getString("b_location"));
			 String depCity = beginLocation.getString("city_code").replaceAll("[^a-zA-Z\\d]", "");
			 
			 JSONObject fjson2 = segJSON.getJSONObject(segJSON.size()-1);
			 JSONObject endLocation = JSON.parseObject(fjson2.getString("e_location"));
			 String arrCity = endLocation.getString("city_code").replaceAll("[^a-zA-Z\\d]", "");
			 arr[0] = depCity;
			 arr[1] = arrCity;
		}
	
		return arr;
	}
	
	
	
	/**
	 * @throws ParseException 
	 * 
	 * @Title: getFlightDetail 
	 * @Description: 鑾峰彇鑸彮璇︽儏淇℃伅
	 * @param flightDetail
	 * @param jsonArray
	 * @param ojson
	 * @param arg1
	 * @return聽聽聽 聽
	 * @throws
	 */
	private FlightDetail getFlightDetail(FlightDetail flightDetail,JSONArray jsonArray,JSONObject ojson,FlightSearchParam arg1) throws ParseException{
		if(jsonArray.size() >= 0){
			JSONObject priceJson = jsonArray.getJSONObject(0);
			flightDetail.setDepdate(getDate(arg1.getDepDate()));
			flightDetail.setMonetaryunit(JSON.parseObject(priceJson.getString("currency")).getString("code"));
			flightDetail.setPrice(priceJson.getDouble("total_amount"));
	//		flightDetail.setDepcity(arg1.getDep());
	//		flightDetail.setArrcity(arg1.getArr());
			flightDetail.setWrapperid(arg1.getWrapperid());
			flightDetail.setTax(priceJson.getDouble("tax"));
			
		}
		return flightDetail;
	}
	
	/*********************************閫氱敤宸ュ叿绫�*******************************************/
	HttpClient httpClient = null;
	protected HttpClient getHttp(FlightSearchParam param){
		if(httpClient == null){
			httpClient =  new QFHttpClient(param, false) ;
		}
		
		return httpClient ;
		
	}
	
	/**
	 * 杩斿洖瑙ｆ瀽鍚庣殑date瀵硅薄
	 * @param date--鏃ユ湡瀛楃涓�yyyy-MM-dd
	 * @return
	 * @throws ParseException
	 */
	public static Date getDate(String date) throws ParseException{
		return new SimpleDateFormat("yyyy-MM-dd").parse(date) ;
	}
	
	public static Date forMatDate(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}

	/**
	 * 
	 * @param dateStr
	 * @return 鑾峰彇骞淬�鏈堛�鏃�
	 */
	private static String[] getYearMonthDay(String dateStr){
		String[] arr = new String[3];
		String year = dateStr.substring(0, dateStr.indexOf("-"));
		String month = dateStr.substring(dateStr.indexOf("-")+1,dateStr.lastIndexOf("-"));
		String day = dateStr.substring(dateStr.lastIndexOf("-")+1,dateStr.length());
		arr[0] = year;
		arr[1] = month;
		arr[2] = day;
		return arr;
	}
	
	public static String getDateFromTime(String strDate, String time1, String time2) {

		Date date = forMatDate(strDate);
		String hour1 = time1.substring(0, 2);
		String hour2 = time2.substring(0, 2);
		int day = Integer.parseInt(hour1) > Integer.parseInt(hour2) ? 1 : 0;
		System.out.println("strDate: " + strDate);
		System.out.println("Date: " + date);
		Calendar now = Calendar.getInstance();
		now.setTime(date);
		now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(now.getTime());
		
	}
	
	/*************************************SSL璇佷功璁よ瘉澶勭悊**********************************************/
	
	/**
	 * 蹇界暐SSL璁よ瘉
	 * @author kevin
	 *
	 */
	class MySSLSocketFactory implements ProtocolSocketFactory {  
		  
		     private SSLContext sslcontext = null;  
		   
		     private SSLContext createSSLContext() {  
		         SSLContext sslcontext = null;  
		         try {  
		             sslcontext = SSLContext.getInstance("SSL");  
		             sslcontext.init(null,  
		                     new TrustManager[] { (TrustManager) new TrustAnyTrustManager() },  
		                     new java.security.SecureRandom());  
		         } catch (NoSuchAlgorithmException e) {  
		             e.printStackTrace();  
		         } catch (KeyManagementException e) {  
		             e.printStackTrace();  
		         }  
		         return sslcontext;  
		     }  
		   
		     private SSLContext getSSLContext() {  
		         if (this.sslcontext == null) {  
		             this.sslcontext = createSSLContext();  
		         }  
		         return this.sslcontext;  
		     }  
		   
		     public Socket createSocket(Socket socket, String host, int port,  
		             boolean autoClose) throws IOException, UnknownHostException {  
		         return getSSLContext().getSocketFactory().createSocket(socket, host,  
		                 port, autoClose);  
		     }  
		   
		     @Override
		     public Socket createSocket(String host, int port) throws IOException,  
		             UnknownHostException {  
		         return getSSLContext().getSocketFactory().createSocket(host, port);  
		     }  
		   
		     @Override
		     public Socket createSocket(String host, int port, InetAddress clientHost,  
		             int clientPort) throws IOException, UnknownHostException {  
		         return getSSLContext().getSocketFactory().createSocket(host, port,  
		                 clientHost, clientPort);  
		     }  
		   
		     @Override
		     public Socket createSocket(String host, int port, InetAddress localAddress,  
		             int localPort, HttpConnectionParams params) throws IOException,  
		             UnknownHostException, ConnectTimeoutException {  
		         if (params == null) {  
		             throw new IllegalArgumentException("Parameters may not be null");  
		         }  
		         int timeout = params.getConnectionTimeout();  
		         SocketFactory socketfactory = getSSLContext().getSocketFactory();  
		         if (timeout == 0) {  
		             return socketfactory.createSocket(host, port, localAddress,  
		                     localPort);  
		         } else {  
		             Socket socket = socketfactory.createSocket();  
		             SocketAddress localaddr = new InetSocketAddress(localAddress,  
		                     localPort);  
		             SocketAddress remoteaddr = new InetSocketAddress(host, port);  
		             socket.bind(localaddr);  
		             socket.connect(remoteaddr, timeout);  
		             return socket;  
		         }  
		     }  
		 } 
	
	/**
	 * 蹇界暐SSL璁よ瘉
	 * @author kevin
	 *
	 */
	 private static class TrustAnyTrustManager implements X509TrustManager {  
	  	  @Override
	       public void checkClientTrusted(X509Certificate[] chain, String authType)  
	               throws CertificateException {  
	       }  
	  	  @Override
	       public void checkServerTrusted(X509Certificate[] chain, String authType)  
	               throws CertificateException {  
	       }  
	 
	       public X509Certificate[] getAcceptedIssuers() {  
	           return new X509Certificate[] {};  
	       }  
   }



}
