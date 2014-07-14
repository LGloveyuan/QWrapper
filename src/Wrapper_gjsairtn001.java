
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
 * @ClassName: Wrapper_gjdairju001 
 * @Description:  双程机票抓取
 * @author: LG
 * @date 2014-6-5 
 *
 */
public class Wrapper_gjsairtn001 implements QunarCrawler {
	
	
	public static final int POST = 0;
	
	public static final int GET = 1;
	
	public static final String HOME_URL = "http://www.airtahitinui.com.au/";
	
	private static Map<String,String> citys = new HashMap<String, String>();
	
	public static void main(String[] args) {

		FlightSearchParam searchParam = new FlightSearchParam();
		searchParam.setDep("SYD");
		searchParam.setArr("PPT"); //CDG
		searchParam.setDepDate("2014-08-15");
		searchParam.setRetDate("2014-09-15");
		searchParam.setTimeOut("60000");
		searchParam.setToken("");
		searchParam.setWrapperid("gjsairtn001");
		
		generateCity(citys);

		String html = new Wrapper_gjsairtn001().getHtml(searchParam);
		System.out.println("HTML: " + html);
		
		ProcessResultInfo result = new ProcessResultInfo();

		result = new Wrapper_gjsairtn001().process(html, searchParam);
		
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
		    bookingInfo.setMethod("get");
		    
		    bookingResult.setData(bookingInfo);
		    bookingResult.setRet(true);
		    
		    return bookingResult;		    
		}

	public String getHtml(FlightSearchParam searchParam) {
		
		try {
			String html = getResultHtml(getHttp(searchParam), GET,SEARCH_FLIGHT_URL,getSearchParamMapForSingle(searchParam));
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
			
			GetMethod get1 = new QFGetMethod(SEARCH_FLIGHT_URL);

			httpMethod.setFollowRedirects(false);
			//httpMethod.setFollowRedirects(true);
			String cookie = StringUtils.join(client.getState().getCookies(),"; ");
			client.getState().clearCookies();
			get1.addRequestHeader("Cookie",cookie);
			client.executeMethod(get1);
			return get1.getResponseBodyAsString() ;
			
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
	
	public static Map<String,String> getSearchParamMapForSingle(FlightSearchParam searchParam){
		return getSearchParamMap(searchParam,false);
	}

	private static Map<String,String> getSearchParamMap(FlightSearchParam searchParam,boolean isRound){
		Map<String,String> param = new HashMap<String,String>() ;

		param.put("depart",searchParam.getDep());
		param.put("dest.1",searchParam.getArr());
		param.put("trip_type","return");
		param.put("date.0", processDate(searchParam.getDepDate())); 
		param.put("date.1", processDate(searchParam.getRetDate())); 
		param.put("persons.0","1");
		param.put("persons.1","0");
		param.put("persons.2","0");
		param.put("date_flexibility","fixed");
		param.put("pricing_type","lowest%20available");
		param.put("fare_description","normal");
		param.put("travel_class","E");
		param.put("origin","AU");
		
		return param ;
	}
	
	
	public ProcessResultInfo process(String html, FlightSearchParam arg1) {
		
		generateCity(citys);
		ProcessResultInfo result = new ProcessResultInfo();
		
		String departure_html = org.apache.commons.lang.StringUtils.substringBetween(html, "DEPARTURE FLIGHT OPTIONS", "Clear Selection");
		String return_html = org.apache.commons.lang.StringUtils.substringBetween(html, "RETURN FLIGHT OPTIONS", "Clear Selection");
		
		if(null == departure_html || null == return_html)
		{
			result.setRet(false);
			result.setStatus(Constants.NO_RESULT);
			return result;
		}
        
        departure_html = departure_html.replaceAll("[\\s\"]", "");
        
        return_html = return_html.replaceAll("[\\s\"]", "");
        
		if ("Exception".equals(html) || null == html) {
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;
		}
		
		if(html.contains("Sorry. There are no flights available that meet your request. Please try an alternative date."))
		{
			result.setRet(false);
			result.setStatus(Constants.INVALID_DATE);
			return result;
		}
        
		 FlightSearchParam serachParam = arg1;
	        
	        List<OneWayFlightInfo> depFlightList = new ArrayList<OneWayFlightInfo>();
	        List<OneWayFlightInfo> retFlightList = new ArrayList<OneWayFlightInfo>();
	        
	        List<RoundTripFlightInfo> roundList = new ArrayList<RoundTripFlightInfo>();
	        
	       
	        
			if ("Exception".equals(html) || null == html || null == departure_html || null == return_html) {
				result.setRet(false);
				result.setStatus(Constants.CONNECTION_FAIL);
				return result;
			}
	        new Wrapper_gjsairtn001().subProcess(departure_html, depFlightList, 0, serachParam);
	        new Wrapper_gjsairtn001().subProcess(return_html, retFlightList, 1, serachParam);
	        
	        if(null == depFlightList || null == retFlightList)
	        {
	        	result.setRet(false);
	        	result.setStatus(Constants.NO_RESULT);
	        	return result;
	        }
	        
	        for(OneWayFlightInfo dep:depFlightList){
				for(OneWayFlightInfo ret:retFlightList){
					
					RoundTripFlightInfo roundInfo = new RoundTripFlightInfo() ;
					
					FlightDetail detail = cloneDetail(dep.getDetail()) ;
					detail.setPrice(detail.getPrice() + ret.getDetail().getPrice()) ;
					
					roundInfo.setDetail(detail) ;//detail
					roundInfo.setInfo(cloneFlightSegementList(dep.getInfo())) ;
					roundInfo.setOutboundPrice(dep.getDetail().getPrice()) ;
					roundInfo.setRetdepdate(ret.getDetail().getDepdate()) ;
					roundInfo.setRetflightno(ret.getDetail().getFlightno()) ; 
					roundInfo.setRetinfo(cloneFlightSegementList(ret.getInfo())) ;
					roundInfo.setReturnedPrice(ret.getDetail().getPrice()) ;
					
					roundList.add(roundInfo) ;
				}
			}
	        
	        
			result.setRet(true);
			result.setStatus(Constants.SUCCESS);
			result.setData(roundList);
			return result;
	}

	private void subProcess(String html,
			List<OneWayFlightInfo> flightList,
			int flag, FlightSearchParam searchParam) {
		
		  String depPort = null;
		  String arrPort = null;
		  String flightNo = null;
		  String depTime = null;
		  String arrTime = null;
		  String depDate = null;
		  String arrDate = null;
		  String monUnit = "USD";
		  
		  FlightSegement seg = null;
		  List<String> flightNos = new ArrayList<String>();
		  FlightDetail flightDetail = new FlightDetail();
		  List<FlightSegement> segs = new ArrayList<FlightSegement>();
		  OneWayFlightInfo baseFlight = new OneWayFlightInfo();
		  
		  Double price = Double.MAX_VALUE;

		  String start = "<trid=rowFM_" + flag + "_0_0";
		  String end = "<tdcolspan=9>";
		  String new_html = org.apache.commons.lang.StringUtils.substringBetween(html, start, end);
		  
		  String[] str = new_html.split("<trid=rowFM_" + flag + "_[0-9]_0");
		  for(int i=0;i<str.length;i++)
		  {
			  String regx_price = "\\SD<br/>(.*?)\\<";
			  Pattern p_price = Pattern.compile(regx_price);
	          Matcher m_price = p_price.matcher(str[i]);
	          while(m_price.find())
	        	{
	        		String _price = m_price.group(1);
	        		double temp_price = Double.MAX_VALUE;
	        		if(!"N/A".equals("_price")&&null!=_price)
	        		{
	        			temp_price = Double.parseDouble(_price.replaceAll(",", ""));
	        		}
	        		if(price > temp_price)
	        			price = temp_price;
	        	}
	          if(Double.MAX_VALUE == price)
	        	  continue;

			  String regx1 = "\\<spanclass='FlightNumberInTable'>(.*?)\\</td></tr>";
	          Pattern p1 = Pattern.compile(regx1);
	          Matcher m1 = p1.matcher(str[i]);
	          int x = 0;
	          while(m1.find())
	          {
	        	seg = new FlightSegement();
	        	
	        	String info = m1.group(1);
	        	
	        	String regx2 = "\\<tdclass=step2CellflightInfo_middle>(.*?)\\<br/>";
	        	Pattern p2 = Pattern.compile(regx2);
	        	Matcher m2 = p2.matcher(info);
	        	int j = 0;
	        	while(m2.find())
	        	{
	        		String port = m2.group(1);
	        		if(j == 0) depPort = processPort(port);
	        		else arrPort = processPort(port);
	        		j++;
	        	}

	        	flightNo = org.apache.commons.lang.StringUtils.substringBetween(info, "", "</span>");
	        	flightNos.add(flightNo);
	        	
	        	String regx3 = "\\<br/>(.*?)\\</td>";
	        	Pattern p3 = Pattern.compile(regx3);
	        	Matcher m3 = p3.matcher(info);
	        	int k= 3;
	        	while(m3.find())
	        	{
	        			String time = m3.group(1);
	            		if(time.contains(":") && k>0) depTime = time;
	            		else if(time.contains(":") && k<=0) arrTime = time;  
	            		k--;
	        	}
	        	if(x != 0)
	        	{ 
	        	arrTime = info.substring(info.length() -8 , info.length());
	        	}
	        	x++;
	        	
	        	seg.setDepairport((String) citys.get(depPort));
	        	seg.setArrairport((String) citys.get(arrPort));
	        	seg.setDeptime(processTime(depTime));
	        	seg.setArrtime(processTime(arrTime));
	        	seg.setFlightno(flightNo);
	        	
	        	if(0 == flag){
	        		seg.setDepDate(getDateFromTime(searchParam.getDepDate(), depTime));
					seg.setArrDate(getDateFromTime(searchParam.getDepDate(), arrTime));
	        	}
	        	else if(1 == flag){
	        		seg.setDepDate(getDateFromTime(searchParam.getRetDate(), depTime));
					seg.setArrDate(getDateFromTime(searchParam.getRetDate(), arrTime));
	        	}
	        	
	        	segs.add(seg);
	          }
	          flightDetail.setDepcity(searchParam.getDep());
	          flightDetail.setArrcity(searchParam.getArr());
	          flightDetail.setDepdate(forMatDate(searchParam.getDepDate()));
	          flightDetail.setFlightno(flightNos);
	          flightDetail.setMonetaryunit(monUnit);
	          flightDetail.setTax(0);
	          flightDetail.setPrice(price);
	          flightDetail.setWrapperid(searchParam.getWrapperid());

	          baseFlight.setDetail(flightDetail);
	          baseFlight.setInfo(segs);
	          
	          flightList.add(baseFlight);
		  }
	}
	
	
	HttpClient httpClient = null;
	protected HttpClient getHttp(FlightSearchParam param){
		if(httpClient == null){
			httpClient =  new QFHttpClient(param, false) ;
		}
		
		return httpClient ;
		
	}
	
	public static String processPort(String port) {
		if (port.contains("<b>")) {
			return port.substring(3, port.length() - 4);
		} else
			return port;
	}

	public static String processTime(String time) {
		return time.substring(3, 8);
	}

	public static String processDate(String s) {
		String[] str = s.split("-");
		String result = "";
		
		switch (Integer.parseInt(str[2])) {
		case 1:
			result += "1";
			break;
		case 2:
			result += "2";
			break;
		case 3:
			result += "3";
			break;
		case 4:
			result += "4";
			break;
		case 5:
			result += "5";
			break;
		case 6:
			result += "6";
			break;
		case 7:
			result += "7";
			break;
		case 8:
			result += "8";
			break;
		case 9:
			result += "9";
			break;
		default:
			result += str[2];
			break;
		}
		
		switch(Integer.parseInt(str[1]))
		{
		case 1:result += "Jan";break;
		case 2:result += "Feb";break;
		case 3:result += "Mar";break;
		case 4:result += "Apr";break;
		case 5:result += "May";break;
		case 6:result += "Jun";break;
		case 7:result += "Jul";break;
		case 8:result += "Aug";break;
		case 9:result += "Sep";break;
		case 10: result += "Oct";break;
		case 11: result += "Nov";break;
		case 12: result += "Dec";break;
		}

		return result;
	}

	public static Date forMatDate(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}

	public static String getDateFromTime(String strDate, String time) {

		Date date = forMatDate(strDate);
		int day = getDay(date, time);
		Calendar now = Calendar.getInstance();
		now.setTime(date);
		now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(now.getTime());
		
	}

private static int getDay(Date date, String time) {
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int current_day = cal.get(Calendar.DAY_OF_WEEK) - 1;
		int week = 0;
		String temp = time.substring(0, 3);
		if("Mon".equals(temp))
		{
			week = 1;
		}
		else if("Tue".equals(temp))
		{
			week = 2;
		}
		else if("Wed".equals(temp))
		{
			week = 3;
		}
		else if("Thu".equals(temp))
		{
			week = 4;
		}
		else if("Fri".equals(temp))
		{
			week = 5;
		}
		else if("Sat".equals(temp))
		{
			week = 6;
		}
		else if("Sun".equals(temp))
		{
			week = 0;
		}
		int sub = week - current_day;
		return (sub + 7) % 7;
	}
	private static void generateCity(Map<String,String> citys)
	{
		citys.put("Sydney", "SYD");citys.put("Melbourne", "MEL");
		citys.put("Brisbane", "BNE");citys.put("LosAngeles", "LAX");
		citys.put("Adelaide", "ADL");citys.put("Cairns", "CNS");
		citys.put("Darwin", "DRW");citys.put("Perth", "PER");
		citys.put("Canberra", "CBR");citys.put("Tikehau", "TIH");
		citys.put("Manihi", "XMH");citys.put("Atlanta,GA", "ATL");
		citys.put("Austin,TX", "AUS");citys.put("Boston,MA", "BOS");
		citys.put("Charlotte,NC", "CLT");citys.put("Phoenix,AZ", "PHX");
		citys.put("Portland,OR", "PDX");citys.put("Reno,NV", "RNO");
		citys.put("SaltLakeCity,UT", "SLC");citys.put("SanDiego,CA", "SAN");
		citys.put("SanFrancisco,CA", "SFO");citys.put("SanJose,CA", "SJC");
		citys.put("SantaBarbara,CA", "SBA");citys.put("Marseille", "MRS");
		citys.put("MarseilleTGV", "XRF");citys.put("MetzLorraineTGV", "XZI");
		citys.put("Montpellier", "MPL");citys.put("MontpellierTGV", "XPJ");
		citys.put("Nantes", "NTE");citys.put("NantesTGV", "QJZ");
		citys.put("Nice", "NCE");citys.put("Chicago,IL", "ORD");
		citys.put("Seattle,WA", "SEA");citys.put("GoldCoast", "OOL");
		citys.put("Cincinnati,OH", "CVG");citys.put("St.Louis,MO", "STL");
		citys.put("Auckland", "AKL");citys.put("Christchurch", "CHC");
		citys.put("Wellington", "WLG");citys.put("Queenstown", "ZQN");
		citys.put("Dallas,TX", "DFW");citys.put("Denver,CO", "DEN");
		citys.put("Detroit,MI", "DTW");citys.put("Ft.Lauderdale,FL", "FLL");
		citys.put("Honolulu,HI", "HNL");citys.put("Washington,DC", "WAS");
		citys.put("Paris", "CDG");citys.put("AixenprovenceTGV", "QXB");
		citys.put("AngersTGV", "QXG");citys.put("NimesTGV", "ZYN");
		citys.put("Pau", "PUF");citys.put("PoitiersTGV", "XOP");
		citys.put("ReimsTGV", "XIZ");citys.put("RennesTGV", "ZFJ");
		citys.put("Strasbourg", "SXB");citys.put("StrasbourgTGV", "XWG");
		citys.put("Philadelphia,PA", "PHL");citys.put("Papeete", "PPT");
		citys.put("BoraBora", "BOB");citys.put("Fakarava", "FAV");
		citys.put("Huahine", "HUH");citys.put("Moorea", "MOZ");
		citys.put("Raiatea", "RFP");citys.put("Rangiroa", "RGI");
		citys.put("Houston,TX", "IAH");citys.put("LasVegas,NV", "LAS");
		citys.put("Memphis,TN", "MEM");citys.put("Miami,FL", "MIA");
		citys.put("Minneapolis,MN", "MSP");citys.put("NewYork,NY", "NYC");
		citys.put("Orlando,FL", "ORL");citys.put("AvignonTGV", "AVN");
		citys.put("Brest", "BES");citys.put("Bordeaux", "BOD");
		citys.put("BordeauxTGV", "ZFQ");citys.put("LeMansTGV", "ZLN");
		citys.put("LilleTGV", "XDB");citys.put("Lyon", "LYS");
		citys.put("LyonTGV", "XYD");citys.put("ToulonTGV", "XZV");
		citys.put("Toulouse", "TLS");citys.put("ToursTGV", "XSH");
		citys.put("ValenceTGV", "XHK");citys.put("Tokyo", "NRT");
		citys.put("Tahiti–Papeete", "PPT");

	}
	

	private static FlightDetail cloneDetail(FlightDetail oldDetail){
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


	private static List<FlightSegement> cloneFlightSegementList(List<FlightSegement> segs){
		
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

	public static String SEARCH_FLIGHT_URL = "https://secure.airtahitinui.com/OnlineBooking.aspx" ;

}
