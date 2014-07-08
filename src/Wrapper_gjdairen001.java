import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
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
 * @ClassName: Wrapper_gjdairen001 
 * @Description:  Air Dolomiti单程机票抓取
 * @author: LG
 * @date 2014-7-9 
 *
 */
public class Wrapper_gjdairen001 implements QunarCrawler {
	
	
	public static final int POST = 0;
	
	public static final int GET = 1;
	
	public static final String HOME_URL = "http://www.airdolomiti.eu/";
	
	public static String SEARCH_FLIGHT_URL = "https://wftc2.e-travel.com/plnext/FPCairdolomiti/Override.action" ;
	
	public static void main(String[] args) {

		FlightSearchParam searchParam = new FlightSearchParam();
		searchParam.setDep("VCE");
		searchParam.setArr("MUC");
		searchParam.setDepDate("2014-07-26");
		searchParam.setTimeOut("60000");
		searchParam.setToken("");
		searchParam.setWrapperid("gjdairen001");
		

		String html = new Wrapper_gjdairen001().getHtml(searchParam);
		System.out.println("HTML: " + html);
		
		ProcessResultInfo result = new ProcessResultInfo();

		result = new Wrapper_gjdairen001().process(html, searchParam);
		
		if (result.isRet() && result.getStatus().equals(Constants.SUCCESS)) {
			List<OneWayFlightInfo> flightList = (List<OneWayFlightInfo>) result
					.getData();
			for (OneWayFlightInfo in : flightList) {
				System.out.println("************" + in.getInfo().toString());
				System.out.println("++++++++++++" + in.getDetail().toString());

			}
			
			//测试
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
		    bookingInfo.setMethod("get");
		    
		    bookingResult.setData(bookingInfo);
		    bookingResult.setRet(true);
		    
		    return bookingResult;		    
		}

	public String getHtml(FlightSearchParam searchParam) {
		
		try {
			String html = getResultHtml(getHttp(searchParam), GET,SEARCH_FLIGHT_URL,getSearchParamMapForSingle(searchParam));
		
			System.out.println("htmlAll: " + html);
			
		    return html;
		} catch (Exception e) {
		}
			
		return "Exception";
		
	}

    private String getResultHtml(HttpClient client ,int method,String uri,Map<String,String> paramMap) {
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

    private static Map<String,String> getSearchParamMapForSingle(FlightSearchParam searchParam){
		Map<String,String> param = new HashMap<String,String>() ;

		param.put("B_LOCATION_1",searchParam.getDep());
		param.put("E_LOCATION_1",searchParam.getArr());
		param.put("B_LOCATION_2",searchParam.getArr());
		param.put("E_LOCATION_2",searchParam.getDep());
		param.put("B_DATE_1",searchParam.getDepDate().replaceAll("-", "")+"0000");
		param.put("B_DATE_2",searchParam.getDepDate().replaceAll("-", "")+"0000");
		param.put("TRIP_TYPE","O");
		
		param.put("EXTERNAL_ID","BOOKING");
		param.put("SITE","BAPRBAPR");
		param.put("LANGUAGE","GB");
		param.put("SO_SITE_FP_PRICING_TYPE","O");
		param.put("TRIP_FLOW","YES");
		param.put("ARRANGE_BY","D");
		param.put("SO_SITE_MOP_CALL_ME","FALSE");
		param.put("SO_SITE_USE_SITE_FEE","TRUE");
		param.put("REFRESH","0");
		param.put("B_ANY_TIME_1","TRUE");
		param.put("B_ANY_TIME_2","TRUE");
		param.put("EMBEDDED_TRANSACTION","FlexPricerAvailability");
		param.put("TRAVELLER_TYPE_1","ADT");
		param.put("HAS_INFANT_1","FALSE");
		param.put("PRICING_TYPE","C");
		param.put("DISPLAY_TYPE","2");
		param.put("COMMERCIAL_FARE_FAMILY_1","NEWCFF");
		param.put("DATE_RANGE_VALUE_1","1");
		param.put("DATE_RANGE_VALUE_2","1");
		param.put("DATE_RANGE_QUALIFIER_1","C");
		param.put("DATE_RANGE_QUALIFIER_2","C");
		param.put("SO_SITE_AVAIL_SERVICE_FEE","TRUE");
		return param ;
	}
	
	
	public ProcessResultInfo process(String html, FlightSearchParam arg1) {
		
		html = html.replaceAll("[\\s\"]", "");
		System.out.println("clean_html: " + html);
		ProcessResultInfo result = new ProcessResultInfo();
	
		String departure_html = org.apache.commons.lang.StringUtils.substringBetween(html, "<tableid=fpcTableFareFamilyContent_out", "<divid=tooltipMinWidthHolder>");
		
		System.out.println("departure_html: " + departure_html);
        
        FlightSearchParam serachParam = arg1;
        
		if ("Exception".equals(html) || null == html || null == departure_html) {
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;
		}
		
		List<OneWayFlightInfo> depFlightList = new ArrayList<OneWayFlightInfo>();
		depFlightList = subProcess(departure_html, depFlightList, 0, serachParam);
        
        result.setRet(true);
        result.setData(depFlightList);
		result.setStatus(Constants.SUCCESS);
		System.out.println("result: " + result);
		return result;
	}

	private static List<OneWayFlightInfo> subProcess(String html,
			List<OneWayFlightInfo> flightList,
			int flag, FlightSearchParam searchParam) {
		
		  String depPort = null;
		  String arrPort = null;
		  String flightNo = null;
		  String depTime = null;
		  String arrTime = null;
		  String depDate = null;
		  String arrDate = null;
		  String monUnit = "EUR";
		  
		  FlightSegement seg = null;
		  
        
		  double price = Double.MAX_VALUE;
		 // float temp_price = Float.MAX_VALUE;
		  // 将多条航班信息提取出来保存在数组里
		  String[] str = html.split("<tablecellpadding=0cellspacing=0border=0width=100%>");

		  // 处理每一条航班信息
		  for(int i=1;i<str.length;i++)
		  {
			  List<FlightSegement> segs = new ArrayList<FlightSegement>();
			  OneWayFlightInfo baseFlight = new OneWayFlightInfo();
			  FlightDetail flightDetail = new FlightDetail();
			  List flightNos = new ArrayList<String>();
			  
			  System.out.println("record" + i + ": " + str[i]);
			  String _price = org.apache.commons.lang.StringUtils.substringBetween(str[i], "&euro;", "</td>"); // 提取价格
			  System.out.println("_price: " + _price);
			  if(!"SoldOut".equals("_price") && null!=_price)
			  {
				  price = Double.parseDouble(_price.replaceAll(",", ""));
			  }
			  else{continue;}
			  
			  // 提取航班号所在的字符串
			  String flightName = org.apache.commons.lang.StringUtils.substringBetween(str[i], "<spanclass=nameHighlight>", "</span>");
			  System.out.println("flightName: " + flightName);
			  // 提取航班号
			  flightNo = org.apache.commons.lang.StringUtils.substringBetween(flightName, "(", ")"); 
			  System.out.println("flightNo: " + flightNo);
			  flightNos.add(flightNo);

			  // 提取出发时间和到达时间
			  depTime = org.apache.commons.lang.StringUtils.substringBetween(str[i],"<tdclass=textBoldnowraptopLinestyle=width:10%;text-align:center;padding-left:5px;>", "</td>");
			  arrTime = org.apache.commons.lang.StringUtils.substringBetween(str[i],"<tdclass=textBoldnowrapbottomLinestyle=width:10%;text-align:center;padding-left:5px;>", "</td>");
			  System.out.println("depTime: " + depTime);
			  System.out.println("arrTime: " + arrTime);
			  // 提取出发机场所在字符串
			  String strDepPort = org.apache.commons.lang.StringUtils.substringBetween(str[i], "<tdclass=nowraptopLinestyle=width:25%;padding-left:5px;>", "</a>");
			  System.out.println("strDepPort: " + strDepPort);
			  // 提取出发机场
			  depPort = strDepPort.substring(strDepPort.length()-3, strDepPort.length());
			  System.out.println("depPort:" + depPort);
			  // 提取到达机场所在字符串
			  String strArrPort = org.apache.commons.lang.StringUtils.substringBetween(str[i], "<tdclass=nowrapbottomLinestyle=width:25%;padding-left:5px;>", "</a>");
			  System.out.println("strArrPort: " + strArrPort);
			  // 提取到达机场
			  arrPort = strArrPort.substring(strArrPort.length()-3, strArrPort.length());
			  System.out.println("arrPort:" + arrPort);
			  // 封装信息
		        seg = new FlightSegement();
		        
	        	seg.setDepairport(depPort);
	        	seg.setArrairport(arrPort);
	        	seg.setDeptime(depTime);
	        	seg.setArrtime(arrTime);
	        	seg.setFlightno(flightNo);
	        	// 封装出发日期和到达日期
	        	if(0 == flag){
	        		seg.setDepDate(searchParam.getDepDate());
					seg.setArrDate(getDateFromTime(searchParam.getDepDate(), depTime, arrTime));	
	        	}
	        	else if(1 == flag){
	        		seg.setDepDate(searchParam.getRetDate());
					seg.setArrDate(getDateFromTime(searchParam.getRetDate(), depTime, arrTime));
	        	}
	        	
	        	segs.add(seg);
	        	
	        	flightDetail.setDepcity(searchParam.getDep());
		        flightDetail.setArrcity(searchParam.getArr());
		        if(0 == flag){
		        	flightDetail.setDepdate(forMatDate(searchParam.getDepDate()));	
		        }
		        else if(1 == flag){
		        	flightDetail.setDepdate(forMatDate(searchParam.getRetDate()));
		        }
		        flightDetail.setFlightno(flightNos);
		        flightDetail.setMonetaryunit(monUnit);
		        flightDetail.setTax(0);
		        flightDetail.setPrice(price);
		        flightDetail.setWrapperid(searchParam.getWrapperid());
		        
		        baseFlight.setDetail(flightDetail);
		        baseFlight.setInfo(segs);
		        
		        flightList.add(baseFlight);
	          }
		  return flightList;
	            
		  }
		  
	
	
	HttpClient httpClient = null;
	protected HttpClient getHttp(FlightSearchParam param){
		if(httpClient == null){
			httpClient =  new QFHttpClient(param, false) ;
		}
		
		return httpClient ;
		
	}
	
	public static Date forMatDate(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
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

	public static FlightDetail cloneDetail(FlightDetail oldDetail){
		FlightDetail detail = new FlightDetail() ;
		detail.setArrcity(oldDetail.getArrcity()) ;
		detail.setDepcity(oldDetail.getDepcity()) ;
		detail.setDepdate(oldDetail.getDepdate()) ;
		detail.setFlightno(oldDetail.getFlightno()) ;
		detail.setMonetaryunit(oldDetail.getMonetaryunit()) ;
		detail.setTax(oldDetail.getTax()) ;
		detail.setPrice(oldDetail.getPrice()) ;
		detail.setWrapperid(oldDetail.getWrapperid()) ;
		
		return detail ;
	}


	public static List<FlightSegement> cloneFlightSegementList(List<FlightSegement> segs){
		
		List<FlightSegement> segList = new  ArrayList<FlightSegement>() ;
		for(FlightSegement seg : segs){
			FlightSegement s = new FlightSegement() ;
			s.setDepairport(seg.getDepairport()) ;
			s.setDepDate(seg.getDepDate()) ;
			s.setDeptime(seg.getDeptime()) ;
			s.setArrairport(seg.getArrairport()) ;
			s.setArrDate(seg.getArrDate()) ;
			s.setArrtime(seg.getArrtime()) ;
			s.setFlightno(seg.getFlightno()) ;
			segList.add(s) ;
		}
		return segList ;
	}

}
