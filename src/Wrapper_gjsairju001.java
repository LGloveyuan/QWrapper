import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.BaseFlightInfo;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.OneWayFlightInfo;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.bean.search.RoundTripFlightInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.exception.QFHttpClientException;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;
/**
 * 
 * @ClassName: Wrapper_gjsairju001 
 * @Description:  往返机票抓取实现类
 * @author: LG
 * @date 2014-6-5 上午06:58:38 
 *
 */
public class Wrapper_gjsairju001 implements QunarCrawler {

	public static void main(String[] args) {

		FlightSearchParam searchParam = new FlightSearchParam();
		searchParam.setDep("OTP");
		searchParam.setArr("TIV");
		searchParam.setDepDate("2014-09-11");
		searchParam.setRetDate("2014-09-17");
		searchParam.setTimeOut("60000");
		searchParam.setToken("");
		searchParam.setWrapperid("gjsairju001");

		String html = new Wrapper_gjsairju001().getHtml(searchParam);

		ProcessResultInfo result = new ProcessResultInfo();
		result = new Wrapper_gjsairju001().process(html, searchParam);
		if (result.isRet() && result.getStatus().equals(Constants.SUCCESS)) {
			List<RoundTripFlightInfo> flightList = (List<RoundTripFlightInfo>) result
					.getData();
			for (RoundTripFlightInfo in : flightList) {
				System.out.println("************" + in.getInfo().toString());
				System.out.println("++++++++++++" + in.getDetail().toString());

			}
		} else {
			System.out.println(result.getStatus());
		}
	}

	@Override
	public BookingResult getBookingInfo(FlightSearchParam arg0) {
		String bookingUrlPre = "http://www.airserbia.com/booking.php";
		BookingResult bookingResult = new BookingResult();
		
		String depYM = arg0.getDepDate().substring(0, arg0.getDepDate().lastIndexOf("-"));
		String depDD = arg0.getDepDate().substring(arg0.getDepDate().lastIndexOf("-")+1,arg0.getDepDate().length());
		String retYM = arg0.getRetDate().substring(0, arg0.getRetDate().lastIndexOf("-"));
		String retDD = arg0.getRetDate().substring(arg0.getRetDate().lastIndexOf("-")+1,arg0.getRetDate().length());
		
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(bookingUrlPre);
		bookingInfo.setMethod("post");
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("tripType", "round_trip");
		map.put("fromCity", arg0.getDep());
		map.put("toCityStart", "");
		map.put("startTimeYM", depYM);
		map.put("startTimeD", depDD);
		map.put("fromCityEnd", "");
		map.put("toCity", arg0.getArr());
		map.put("endTimeYM", retYM);
		map.put("endTimeD", retDD);
		map.put("fareFamily", "ECOFIRST");
		map.put("passengersADT", "1");
		map.put("passengersCHD", "0");
		map.put("passengersINF", "0");
		map.put("submit", "Book+Flight");
		map.put("currentLang", "en");
		bookingInfo.setInputs(map);
		bookingResult.setData(bookingInfo);
		bookingResult.setRet(true);
		return bookingResult;
		
	}

	@Override
	public String getHtml(FlightSearchParam arg0) {
			QFHttpClient httpClient = new QFHttpClient(arg0, false);
			//获取ip及其他变量值
			String[] arr = getIPAndVar(arg0,httpClient);
			if(null != arr && arr.length >= 0){
				 String html = getResultHtml(arr,arg0,httpClient);
				 return html;
			}else {
				return "Exception";
			}
		
	}

	/**
	 * 
	 * @Title: getIP 
	 * @Description: 获取portal_var_3所对应的ip和其他一些变量值
	 * @param arg0 航班检索条件
	 * @param post 
	 * @param httpClient
	 * @return     
	 * @throws
	 */
	private String[] getIPAndVar(FlightSearchParam arg0,QFHttpClient httpClient){
		QFPostMethod post = null;
		String[] arr = new String[5];
		String url = "http://www.airserbia.com/booking.php";
		String depYM = arg0.getDepDate().substring(0, arg0.getDepDate().lastIndexOf("-"));
		String depDD = arg0.getDepDate().substring(arg0.getDepDate().lastIndexOf("-")+1,arg0.getDepDate().length());
		String retYM = arg0.getRetDate().substring(0, arg0.getRetDate().lastIndexOf("-"));
		String retDD = arg0.getRetDate().substring(arg0.getRetDate().lastIndexOf("-")+1,arg0.getRetDate().length());
		NameValuePair[] data = {
				new NameValuePair("tripType", "round_trip"),
				new NameValuePair("fromCity", arg0.getDep()),
				new NameValuePair("toCityStart", ""),
				new NameValuePair("startTimeYM", depYM),
				new NameValuePair("startTimeD", depDD),
				new NameValuePair("fromCityEnd", ""),
				new NameValuePair("toCity", arg0.getArr()),
				new NameValuePair("endTimeYM", retYM),
				new NameValuePair("endTimeD", retDD),
				new NameValuePair("fareFamily", "ECOFIRST"),
				new NameValuePair("passengersADT", "1"),
				new NameValuePair("passengersCHD", "0"),
				new NameValuePair("passengersINF", "0"),
				new NameValuePair("submit", "Book+Flight"),
				new NameValuePair("currentLang", "en")
		};
		
		post = new QFPostMethod(url);
		post.setRequestBody(data);
		String html ="";
		try {
			int status = httpClient.executeMethod(post);
			html = post.getResponseBodyAsString();
		} catch (QFHttpClientException e) {
			e.printStackTrace();
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if (null != post) {
				post.releaseConnection();
			}
		}
		//从html网页里把ip值截取出来
		if(html.indexOf("wrong data") != -1){
			return null;
		}else{
			String ip = html.substring(html.indexOf("portal_var_3\" value=\"")+21, html.indexOf("||Global"));
			String mozilla = org.apache.commons.lang.StringUtils.substringBetween(
					html, "portal_var_5\" value=\"", "\" />");
			arr[0] = ip;
			arr[1] = mozilla;
		    return arr;
		}
	}
	
	/**
	 * 
	 * @Title: getResultHtml 
	 * @Description: 获取机票结果信息的网页信息
	 * @param ip
	 * @param arg0
	 * @param post
	 * @param httpClient
	 * @return     
	 * @throws
	 */
	private String getResultHtml(String[] arr,FlightSearchParam arg0,QFHttpClient httpClient){
		QFPostMethod post = null;
		String url = "http://book.airserbia.com/plnext/AirSERBIA/Override.action";
		String depDate = arg0.getDepDate().replaceAll("-", "")+"0000";
		String retDate = arg0.getRetDate().replaceAll("-", "")+"0000";
		NameValuePair[] data = { new NameValuePair("ENVIRONMENT", "PRODUCTION_JAT"),
				                  new NameValuePair("EMBEDDED_TRANSACTION", "FlexPricerAvailability"), 
				                  new NameValuePair("LANGUAGE", "GB"),
				                  new NameValuePair("SITE", "BFKCBFKC"),
				                  new NameValuePair("TRIP_FLOW", "YES"),
				                  new NameValuePair("DIRECT_LOGIN", "NO"),
				                  new NameValuePair("portal_var_3", arr[0]+"%7C%7CGlobal"),
				                  new NameValuePair("portal_var_4", "1%7C0%7C0%7C0%7C0"),
				                  new NameValuePair("portal_var_5", arr[1]),
				                  new NameValuePair("TRAVELLER_TYPE_1", "ADT"),
				                  new NameValuePair("COMMERCIAL_FARE_FAMILY_1", "ECOFIRST"),
				                  new NameValuePair("PRICING_TYPE", "O"),
				                  new NameValuePair("DISPLAY_TYPE", "1"),
				                  new NameValuePair("EXTERNAL_ID", "booking"),
				                  new NameValuePair("SO_SITE_MOP_CALL_ME", "FALSE"),
				                  new NameValuePair("SO_SITE_MOP_CREDIT_CARD", "FALSE"),
				                  new NameValuePair("SO_SITE_BOOL_ISSUE_ETKT", "TRUE"),
				                  new NameValuePair("SO_SITE_ALLOW_SERVICE_FEE", "0"),
				                  new NameValuePair("SO_SITE_MOP_EXT", "TRUE"),
				                  new NameValuePair("SO_SITE_EXT_PSPURL", "https%3A%2F%2Fpayment.airserbia.com%2Fenter.php"),
				                  new NameValuePair("SO_SITE_EXT_PSPCODE", "airserbia"),
				                  new NameValuePair("SO_SITE_EXT_PSPTYPE", "HTML"),
				                  new NameValuePair("SO_SITE_EXT_MERCHANTID", "DEJAT007"),
				                  new NameValuePair("B_DATE_1", depDate),
				                  new NameValuePair("B_ANY_TIME_1", "TRUE"),
				                  new NameValuePair("B_DATE_2", retDate),
				                  new NameValuePair("TRIP_TYPE", "R"),
				                  new NameValuePair("B_ANY_TIME_2", "TRUE"),
				                  new NameValuePair("E_LOCATION_2", ""),
				                  new NameValuePair("B_LOCATION_2", ""),
				                  new NameValuePair("B_LOCATION_1", arg0.getDep()),
				                  new NameValuePair("E_LOCATION_1", arg0.getArr()),
				                  new NameValuePair("ARRANGE_BY", "D")};
		
		
		post = new QFPostMethod(url);
		post.setRequestBody(data);
		String html ="";
		try {
			int status = httpClient.executeMethod(post);
			 html = org.apache.commons.lang.StringUtils.substringBetween(
					post.getResponseBodyAsString(), "new String('", "')");
			System.out.println(html);
		} catch (QFHttpClientException e) {
			e.printStackTrace();
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if (null != post) {
				post.releaseConnection();
			}
		}
	    return html;
		
	}
	
	public ProcessResultInfo process(String arg0, FlightSearchParam arg1) {
		 String html = arg0;
	        System.out.println(html);
			
			/* ProcessResultInfo中，
			 * ret为true时，status可以为：SUCCESS(抓取到机票价格)|NO_RESULT(无结果，没有可卖的机票)
			 * ret为false时，status可以为:CONNECTION_FAIL|INVALID_DATE|INVALID_AIRLINE|PARSING_FAIL|PARAM_ERROR
			 */
			ProcessResultInfo result = new ProcessResultInfo();
			if ("Exception".equals(html)) {	
				result.setRet(false);
				result.setStatus(Constants.CONNECTION_FAIL);
				return result;			
			}		
			//需要有明显的提示语句，才能判断是否INVALID_DATE|INVALID_AIRLINE|NO_RESULT
			if (html.indexOf("wrong data") != -1) {
				result.setRet(false);
				result.setStatus(Constants.INVALID_DATE);
				return result;			
			}
			

		//	String listSegment = org.apache.commons.lang.StringUtils.substringBetween(html, "\"list_segment\":", ",\"flight_id\"");
			String listFlight = org.apache.commons.lang.StringUtils.substringBetween(html, "\"list_proposed_bound\":", ",\"list_recommendation\"");
			System.out.println(listFlight);
			String listDetail = org.apache.commons.lang.StringUtils.substringBetween(html, "\"list_recommendation\":", ",\"list_date\":");		
			System.out.println(listDetail);
			try {			
				List<RoundTripFlightInfo> roundList = new ArrayList<RoundTripFlightInfo>() ;
				JSONArray flightJson = JSON.parseArray(listFlight);	
				JSONObject oneJson = flightJson.getJSONObject(0);
				JSONObject retJson = flightJson.getJSONObject(1);
				JSONArray oneFlightJSON = JSON.parseArray(oneJson.getString("list_flight"));
				JSONArray retFlightJSON = JSON.parseArray(retJson.getString("list_flight"));
				
				for(int i = 0; i < oneFlightJSON.size(); i++){
					//去程航段
					JSONObject ojson = oneFlightJSON.getJSONObject(i);
					JSONArray segJSON = JSON.parseArray(ojson.getString("list_segment"));
					List<FlightSegement> oneSegs = getFlightSegements(segJSON);
					List<String> flightNos = getFlightNoList(segJSON);
					FlightDetail flightDetail = new FlightDetail();
					flightDetail.setFlightno(flightNos);
					flightDetail.setDepdate(getDepdate(segJSON, true));
					/*flightDetail.setDepcity(arg1.getDep());
					flightDetail.setArrcity(arg1.getArr());
					flightDetail.setWrapperid(arg1.getWrapperid());*/
					
					
					JSONArray djson = JSON.parseArray(listDetail);
					
					for(int j = 0; j < retFlightJSON.size(); j++){
						
						RoundTripFlightInfo  retFlight = new RoundTripFlightInfo();
						
					     //返回航段
						JSONObject rjson = retFlightJSON.getJSONObject(j);
						JSONArray retsegJSON = JSON.parseArray(rjson.getString("list_segment"));
						List<FlightSegement> retSegs = getFlightSegements(retsegJSON);
						List<String> retFlightNos = getFlightNoList(retsegJSON);
						
						JSONArray jsonArray = JSON.parseArray(djson.getJSONObject(0).getString("list_trip_price"));
					    
					    JSONObject priceJson = jsonArray.getJSONObject(0);
					    JSONArray array = JSON.parseArray(priceJson.getString("list_bound_price"));
					    JSONObject object = array.getJSONObject(0);
					    JSONObject retobject = array.getJSONObject(1);
					    
						flightDetail = getFlightDetail(flightDetail,jsonArray,ojson,arg1);
						retFlight.setInfo(oneSegs);
						retFlight.setRetinfo(retSegs);
						retFlight.setDetail(flightDetail) ;//detail
						retFlight.setOutboundPrice(object.getDouble("total_amount")) ;//去程价格
					//	retFlight.setRetdepdate(getDepdate(retsegJSON, false)) ;//返程日期
						retFlight.setRetdepdate(getDate(arg1.getRetDate())) ;//返程日期
						retFlight.setRetflightno(retFlightNos) ; //返程航班号
						retFlight.setReturnedPrice(retobject.getDouble("total_amount")) ;//返程价格
						roundList.add(retFlight);
						
					}
					
				}
				
				//测试
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
	
	/**
	 * 
	 * @Title: getFlightSegements 
	 * @Description: 获取航班信息
	 * @param segJSON
	 * @return     
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
	 * @Description: 获取航班号列表
	 * @param segJSON
	 * @return     
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
	 * @Description: 获取出发日期和返程日期
	 * @param segJSON
	 * @param flag
	 * @return     
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
	 * @throws ParseException 
	 * 
	 * @Title: getFlightDetail 
	 * @Description: 获取航班详情信息
	 * @param flightDetail
	 * @param jsonArray
	 * @param ojson
	 * @param arg1
	 * @return     
	 * @throws
	 */
	private FlightDetail getFlightDetail(FlightDetail flightDetail,JSONArray jsonArray,JSONObject ojson,FlightSearchParam arg1) throws ParseException{
		if(jsonArray.size() >= 0){
			JSONObject priceJson = jsonArray.getJSONObject(0);
			flightDetail.setDepdate(getDate(arg1.getDepDate()));
			flightDetail.setMonetaryunit(JSON.parseObject(priceJson.getString("currency")).getString("code"));
			flightDetail.setPrice(priceJson.getDouble("total_amount"));
			flightDetail.setDepcity(arg1.getDep());
			flightDetail.setArrcity(arg1.getArr());
			flightDetail.setWrapperid(arg1.getWrapperid());
			flightDetail.setTax(priceJson.getDouble("tax"));
			
		}
		return flightDetail;
	}
	
	/**
	 * 返回解析后的date对象
	 * @param date--日期字符串 yyyy-MM-dd
	 * @return
	 * @throws ParseException
	 */
	public static Date getDate(String date) throws ParseException{
		return new SimpleDateFormat("yyyy-MM-dd").parse(date) ;
	}

}
