
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

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
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;


public class Wrapper_gjdairss001 implements QunarCrawler {
	
	
	public static final int POST = 0;
	
	public static final int GET = 1;
	
	public static final String HOME_URL = "http://www.corsair.ca/flight/home";
	
	public static String SEARCH_FLIGHT_URL = "https://www.corsair.ca/flight/content/action" ;
	
	public static void main(String[] args) {

		FlightSearchParam searchParam = new FlightSearchParam();
		searchParam.setDep("ZLN");
		searchParam.setArr("MRU");
		searchParam.setDepDate("2014-10-08");
		searchParam.setTimeOut("60000");
		searchParam.setToken("");
		searchParam.setWrapperid("gjdairss001");
		

		String html = new Wrapper_gjdairss001().getHtml(searchParam);
		System.out.println("HTML: " + html);
		
		ProcessResultInfo result = new ProcessResultInfo();

		result = new Wrapper_gjdairss001().process(html, searchParam);
		
		if (result.isRet() && result.getStatus().equals(Constants.SUCCESS)) {
			List<OneWayFlightInfo> flightList = (List<OneWayFlightInfo>) result
					.getData();
			for (OneWayFlightInfo in : flightList) {
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
		    
		    Protocol myhttps = new Protocol("https", new MySSLSocketFactory(), 443);  
	    	Protocol.registerProtocol("https", myhttps);  

		    BookingInfo bookingInfo = new BookingInfo();
		    bookingInfo.setAction(SEARCH_FLIGHT_URL);
		    bookingInfo.setInputs(getSearchParamMapForSingle(arg0));
		    bookingInfo.setMethod("post");
		    
		    bookingResult.setData(bookingInfo);
		    bookingResult.setRet(true);
		    
		    return bookingResult;		    
		}

	public String getHtml(FlightSearchParam searchParam) {
		Protocol myhttps = new Protocol("https", new MySSLSocketFactory(), 443);  
   	    Protocol.registerProtocol("https", myhttps);  
		QFPostMethod post = null;
		
		HttpClient httpClient = getHttp(searchParam);
		
		try {			
			String html = getResultHtml(getHttp(searchParam), POST,SEARCH_FLIGHT_URL,getSearchParamMapForSingle(searchParam));			
		    return html;
			
		} catch (Exception e) {
		}
			
		return "Exception";
		
	}

    private String getResultHtml(HttpClient client ,int method,String uri,Map<String,String> paramMap) {
    	 Protocol myhttps = new Protocol("https", new MySSLSocketFactory(), 443);  
    	 Protocol.registerProtocol("https", myhttps);  

    	HttpMethod httpMethod = null ;

		try {
			httpMethod = initMethod(method,uri,paramMap) ;
			
			int statusCode = client.executeMethod(httpMethod);  
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
	
			GetMethod get = new QFGetMethod(allUrl);
		
			return get;
		} else {
			throw new Exception("Http method invalid!");
		}
	}

    private static Map<String,String> getSearchParamMapForSingle(FlightSearchParam searchParam){
		Map<String,String> param = new HashMap<String,String>() ;

		param.put("TopLevelNode","1043");
		param.put("ContentNodeID","1043");
		param.put("ContentObjectID","1159");
		param.put("language_code","eng-CA");
		param.put("siteaccess","engca");
		param.put("CallActionHandler","");
		
		param.put("LiaisonID","");   //值是变化的
		param.put("LiaisonID2","");  //值是变化的
		
		param.put("ContentObjectAttribute_ezstring_data_text_258665",searchParam.getDep());
		param.put("ContentObjectAttribute_ezstring_data_text_258666",searchParam.getArr());
		
		param.put("ContentObjectAttribute_ezselect_selected_array_258667[]","1");
		param.put("departuredate",processDateForParam(searchParam.getDepDate()));
		param.put("ContentObjectAttribute_datetime_year_258663",searchParam.getDepDate().substring(0, 4));
		param.put("ContentObjectAttribute_datetime_month_258663",searchParam.getDepDate().substring(5, 7));
		param.put("ContentObjectAttribute_datetime_day_258663",searchParam.getDepDate().substring(8, 10));
		param.put("ContentObjectAttribute_datetime_hour_258663","13");
		param.put("ContentObjectAttribute_datetime_minute_258663","12");
				
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

	public ProcessResultInfo process(String html, FlightSearchParam arg1) {
		
		String resultJson = org.apache.commons.lang.StringUtils.substringBetween(html, "generatedJSon = new String('", "');");
		
		Map<String, Double> prices = new HashMap<String, Double>();
		List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>(); 
		ProcessResultInfo result = new ProcessResultInfo(); 
		String monetaryunit ="CAD";
	
		JSONObject ojson1 = JSON.parseObject(resultJson).getJSONObject("list_tab");
		
		JSONArray proposed_bound = ojson1.getJSONArray("list_proposed_bound");
		JSONArray recommendation = ojson1.getJSONArray("list_recommendation");
		
		if(null == proposed_bound || null == recommendation)
		{
			result.setRet(false);
			result.setStatus(Constants.NO_RESULT);
			return result;
		}
		
		getPriceTax(recommendation, prices);
		
		JSONObject ojson2 = proposed_bound.getJSONObject(0);
		JSONArray flightInfo = ojson2.getJSONArray("list_flight");
		for(int i=0;i<flightInfo.size();i++)
		{
			List<FlightSegement> segs = new ArrayList<FlightSegement>();
			List<String> flightNos = new ArrayList<String>();
			FlightDetail flightDetail = new FlightDetail();
			OneWayFlightInfo baseFlight = new OneWayFlightInfo();
			
			JSONObject ojson3 = flightInfo.getJSONObject(i);
			
			String flight_id = ojson3.getString("flight_id"); 
			double price = prices.get(flight_id + "price");
			double tax = prices.get(flight_id + "tax");
			
			if(0 == price || Double.MAX_VALUE == price) continue;
			JSONArray ajson1 = ojson3.getJSONArray("list_segment");
			for(int j=0;j<ajson1.size();j++)
			{
				JSONObject ojson4 = ajson1.getJSONObject(j);
				
				FlightSegement seg = new FlightSegement();
				String depAirport = null;
				String arrAirport = null;
				String flightNo = null;
				String depTime = null;
				String arrTime = null;
				String depDate = null;
				String arrDate = null;
				
				depAirport = ojson4.getJSONObject("b_location").getString("location_code");
				arrAirport = ojson4.getJSONObject("e_location").getString("location_code");
				
				flightNo = ojson4.getJSONObject("airline").getString("code") + ojson4.getString("flight_number");
				flightNos.add(flightNo);
				
				depTime = ojson4.getString("b_date_formatted_time");
				arrTime = ojson4.getString("e_date_formatted_time");
				
				depDate = ojson4.getString("b_date_date");
				arrDate = ojson4.getString("e_date_date");
				
				seg.setDepairport(depAirport);
				seg.setArrairport(arrAirport);
				seg.setFlightno(flightNo);
				seg.setDeptime(depTime);
				seg.setArrtime(arrTime);
				seg.setDepDate(processDateForSeg(depDate));
				seg.setArrDate(processDateForSeg(arrDate));
				
				segs.add(seg);
			}
			
			flightDetail.setDepcity(arg1.getDep());
			flightDetail.setArrcity(arg1.getArr());
			flightDetail.setFlightno(flightNos);
			flightDetail.setMonetaryunit(monetaryunit);
			flightDetail.setPrice(price);
			flightDetail.setTax(tax);
			flightDetail.setDepdate(processDateForDetail(arg1.getDepDate()));
			flightDetail.setWrapperid(arg1.getWrapperid());
			
			baseFlight.setDetail(flightDetail);
			baseFlight.setInfo(segs);
			
			flightList.add(baseFlight);
		}
		
		result.setData(flightList);
		result.setRet(true);
		result.setStatus(Constants.SUCCESS);
		
		return result;
		
		
	}


       private static Date processDateForDetail(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}

	private static String processDateForParam(String strDate) {
		String rsDate = "";
		if(null != strDate)
		{
			rsDate = strDate.substring(5, 7) + "/" + strDate.substring(8, 10) + "/" + strDate.substring(0, 4);
		}
		return rsDate;
	}
	
	private static String processDateForSeg(String strDate) {
		String rsDate = "";
		if(null != strDate)
		{
			rsDate = strDate.substring(0, 4) + "-" + strDate.substring(4, 6) + "-" + strDate.substring(6, 8);
		}
		return rsDate;
	}

	private static void getPriceTax(JSONArray recommendation,
			Map<String, Double> prices) {
		
		for(int i=0;i<recommendation.size();i++)
		{
			JSONObject ojson1 = recommendation.getJSONObject(i);
			
			JSONArray bound = ojson1.getJSONArray("list_bound");
			JSONArray trip_price = ojson1.getJSONArray("list_trip_price");
			
			JSONObject ojson2 = trip_price.getJSONObject(0);
			
			String _price = ojson2.getString("amount_without_tax");
			double price = Double.MAX_VALUE;
			if(null != _price) price = Double.parseDouble(_price); 
			String _tax = ojson2.getString("tax");
			double tax = 0;
			if(null != _tax) tax = Double.parseDouble(_tax);
			
			JSONObject ojson3 = bound.getJSONObject(0);
			JSONArray flights = ojson3.getJSONArray("list_flight");
			for(int j=0;j<flights.size();j++)
			{
				JSONObject ojson4 = flights.getJSONObject(j);
				String flight_id = ojson4.getString("flight_id");
				Double temp_price = prices.get(flight_id + "price");
				Double temp_tax = prices.get(flight_id + "tax");
				if(null == temp_price || (price+tax) < (temp_price+temp_tax))
				{
					prices.put(flight_id + "price", price);
					prices.put(flight_id + "tax", tax);
				}
			}
		}
		
	}

	HttpClient httpClient = null;
	protected HttpClient getHttp(FlightSearchParam param){
		if(httpClient == null){
			httpClient =  new QFHttpClient(param, false) ;
		}
		
		return httpClient ;
		
	}
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
