package com.gwn.test;

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
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;

/**
 * 
 * @ClassName: Wrapper_gjdairtn001
 * @Description: 大溪地航空单程机票抓取
 * @author: LG
 * @date 2014-6-5
 *
 */
public class Wrapper_gjdairtn001 implements QunarCrawler {

	public static final int POST = 0;

	public static final int GET = 1;

	public static final String HOME_URL = "http://www.airtahitinui.com.au/";
	
	private static Map citys = new HashMap<String, String>();

	public static void main(String[] args) {

		// 搜索条件设置
		FlightSearchParam searchParam = new FlightSearchParam();
		searchParam.setDep("LAX");
		searchParam.setArr("MEL"); // CDG
		searchParam.setDepDate("2014-08-14");
		searchParam.setTimeOut("60000");
		searchParam.setToken("");
		searchParam.setWrapperid("gjdairtn001");
		
		generateCity(citys);
		
		// 获取搜索页面
		String html = new Wrapper_gjdairtn001().getHtml(searchParam);
		System.out.println("HTML: " + html);

		ProcessResultInfo result = new ProcessResultInfo();
		// 信息抓取
		result = new Wrapper_gjdairtn001().process(html, searchParam);

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
			String html = getResultHtml(getHttp(searchParam), GET,
					SEARCH_FLIGHT_URL, getSearchParamMapForSingle(searchParam));

			System.out.println("htmlAll: " + html);
			String htmlResult = org.apache.commons.lang.StringUtils
					.substringBetween(html, "<tr id=\"rowFM_0_0_0\"",
							"<td colspan=\"9\">");

			return htmlResult;
		} catch (Exception e) {
		}

		return "Exception";

	}

	private String getResultHtml(HttpClient client, int method, String uri,
			Map<String, String> paramMap) {
		HttpMethod httpMethod = null;
		try {

			httpMethod = initMethod(method, uri, paramMap);

			int statusCode = client.executeMethod(httpMethod);

			if (statusCode >= 300 && statusCode <= 399) {
				Header location = httpMethod.getResponseHeader("Location");
				String url = "";
				if (location != null) {
					url = location.getValue();
					if (!url.startsWith("http")) {
						url = httpMethod.getURI().getScheme()
								+ "://"
								+ httpMethod.getURI().getHost()
								+ (httpMethod.getURI().getPort() == -1 ? ""
										: (":" + httpMethod.getURI().getPort()))
								+ url;
						return this.requestGet(client, url);
					}
				}
			}

			GetMethod get1 = new QFGetMethod(SEARCH_FLIGHT_URL);
			System.out.println(httpMethod.getStatusCode());

			httpMethod.setFollowRedirects(true);
			String cookie = StringUtils.join(client.getState().getCookies(),
					"; ");
			System.out.println("cookie: " + cookie);
			client.getState().clearCookies();
			get1.addRequestHeader("Cookie", cookie);
			client.executeMethod(get1);
			System.out.println(client.executeMethod(get1));
			return get1.getResponseBodyAsString();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != httpMethod) {
				httpMethod.releaseConnection();
			}
		}
		return null;

	}

	private String requestGet(HttpClient client, String uri) {
		return request(client, GET, uri);
	}

	private String request(HttpClient client, int method, String uri) {
		return getResultHtml(client, method, uri, null);
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
			String allUrl = uri.indexOf("?") > 0 ? (uri + "&" + result) : (uri
					+ "?" + result);
			System.out.println("allUrl: " + allUrl);

			GetMethod get = new QFGetMethod(allUrl);

			return get;
		} else {
			throw new Exception("Http method 不支持!");
		}
	}

	public static Map<String, String> getSearchParamMapForSingle(
			FlightSearchParam searchParam) {
		return getSearchParamMap(searchParam, false);
	}

	private static Map<String, String> getSearchParamMap(
			FlightSearchParam searchParam, boolean isRound) {
		Map<String, String> param = new HashMap<String, String>();

		param.put("depart", searchParam.getDep());
		param.put("dest.1", searchParam.getArr());
		param.put("trip_type", "return");
		// param.put("date.0","14Aug"); //5Jul
		param.put("date.0", processDate(searchParam.getDepDate()));
		param.put("date.1", processDate(searchParam.getDepDate()));
		param.put("persons.0", "1");
		param.put("persons.1", "0");
		param.put("persons.2", "0");
		param.put("date_flexibility", "fixed");
		param.put("pricing_type", "lowest%20available");
		param.put("fare_description", "normal");
		param.put("travel_class", "E");
		param.put("origin", "AU");

		return param;
	}

	public ProcessResultInfo process(String arg0, FlightSearchParam arg1) {

		String html = arg0;
		System.out.println("process html: " + html);
		html = html.replaceAll("[\\s\"]", "");
		System.out.println("newHtml: " + html);
		FlightSearchParam serachParam = arg1;

		String depPort = null;
		String arrPort = null;
		String flightNo = null;
		String depTime = null;
		String arrTime = null;
		String depDate = null;
		String arrDate = null;
		String monUnit = "USD";
		// 获取最低价格
		float price = Float.MAX_VALUE;
		float temp_price = Float.MAX_VALUE;

		List<FlightSegement> segs = null;
		List<String> flightNos = null;
		FlightSegement seg = null;
		FlightDetail flightDetail = new FlightDetail();
		List<OneWayFlightInfo> flightLists = new ArrayList<OneWayFlightInfo>();
		OneWayFlightInfo baseFlight = new OneWayFlightInfo();
		ProcessResultInfo result = new ProcessResultInfo();

		segs = new ArrayList();
		flightNos = new ArrayList();

		if ("Exception".equals(html) || null == html) {
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;
		}

		// 搜索每条中转线路的航班信息
		String[] str = html.split("<trid=rowFM_0_[0-9]_0");
		for (int i = 0; i < str.length; i++) {
			System.out.println("record" + i + ": " + str[i]);

			temp_price = Float.parseFloat(org.apache.commons.lang.StringUtils
					.substringBetween(html, "USD<br/>", "</label>").replaceAll(
							",", ""));
			if (temp_price >= price)
				continue;

			if (null != segs)
				segs.clear();
			if (null != flightNos)
				flightNos.clear();
			flightDetail = new FlightDetail();
			price = temp_price;
			String regx1 = "\\<spanclass='FlightNumberInTable'>(.*?)\\</td></tr>";
			System.out.println("regx: " + regx1);
			Pattern p1 = Pattern.compile(regx1);
			Matcher m1 = p1.matcher(html);
			int x = 0;
			while (m1.find()) {
				seg = new FlightSegement();

				String info = m1.group(1);

				System.out.println("filght info: " + info);
				// 截取每条航班信息的出发城市和到达城市
				String regx2 = "\\<tdclass=step2CellflightInfo_middle>(.*?)\\<br/>";
				Pattern p2 = Pattern.compile(regx2);
				Matcher m2 = p2.matcher(info);
				int j = 0;
				while (m2.find()) {
					String port = m2.group(1);
					if (j == 0)
						depPort = processPort(port);
					else
						arrPort = processPort(port);
					j++;
				}
				// 截取航班列表
				flightNo = org.apache.commons.lang.StringUtils
						.substringBetween(info, "", "</span>");
				flightNos.add(flightNo);
				System.out.println("fightNo: " + flightNo);
				System.out.println("depPort: " + depPort);
				System.out.println("arrPort: " + arrPort);

				// 截取出发时间和到达时间
				String regx3 = "\\<br/>(.*?)\\</td>";
				Pattern p3 = Pattern.compile(regx3);
				Matcher m3 = p3.matcher(info);
				int k = 3;
				while (m3.find()) {
					String time = m3.group(1);
					if (time.contains(":") && k > 0)
						depTime = time;
					else if (time.contains(":") && k <= 0)
						arrTime = time;
					k--;
				}
				System.out.println("depTime: " + depTime);
				if (x != 0) { // 第一条航班的到达时间处理与其他不同
					arrTime = info.substring(info.length() - 8, info.length());
				}
				System.out.println("arrTime: " + arrTime);
				x++;

				seg.setDepairport((String)citys.get(depPort));
				seg.setArrairport((String)citys.get(arrPort));
				seg.setDeptime(processTime(depTime));
				seg.setArrtime(processTime(arrTime));
				seg.setFlightno(flightNo);
				seg.setDepDate(getDateFromTime(serachParam.getDepDate(), depTime));
				seg.setArrDate(getDateFromTime(serachParam.getDepDate(), arrTime));
				segs.add(seg);
			}
			System.out.println("price: " + price);

			System.out.println("dep: " + serachParam.getDep());
			flightDetail.setDepcity(serachParam.getDep());
			flightDetail.setArrcity(serachParam.getArr());
			flightDetail.setDepdate(forMatDate(serachParam
					.getDepDate()));
			flightDetail.setFlightno(flightNos);
			flightDetail.setMonetaryunit(monUnit);
			flightDetail.setTax(0);
			flightDetail.setPrice(price);
			flightDetail.setWrapperid(serachParam.getWrapperid());
		}
		baseFlight.setDetail(flightDetail);
		baseFlight.setInfo(segs);
		flightLists.add(baseFlight);

		result.setRet(true);
		result.setStatus(Constants.SUCCESS);
		result.setData(flightLists);
		System.out.println("result: " + result);
		return result;
	}


	HttpClient httpClient = null;

	protected HttpClient getHttp(FlightSearchParam param) {
		if (httpClient == null) {
			httpClient = new QFHttpClient(param, false);
		}

		return httpClient;

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
		
		switch (str[2]) {
		case "01":
			result += "1";
			break;
		case "02":
			result += "2";
			break;
		case "03":
			result += "3";
			break;
		case "04":
			result += "4";
			break;
		case "05":
			result += "5";
			break;
		case "06":
			result += "6";
			break;
		case "07":
			result += "7";
			break;
		case "08":
			result += "8";
			break;
		case "09":
			result += "9";
			break;
		default:
			result += str[2];
			break;
		}
		
		switch(str[1])
		{
		case "01":result += "Jan";break;
		case "02":result += "Feb";break;
		case "03":result += "Mar";break;
		case "04":result += "Apr";break;
		case "05":result += "May";break;
		case "06":result += "Jun";break;
		case "07":result += "Jul";break;
		case "08":result += "Aug";break;
		case "09":result += "Sep";break;
		case "10": result += "Oct";break;
		case "11": result += "Nov";break;
		case "12": result += "Dec";break;
		}

		return result;
	}

	public static Date forMatDate(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		System.out.println("stroDate: " + strtodate);
		return strtodate;
	}

	public static String getDateFromTime(String strDate, String time) {

		Date date = forMatDate(strDate);
		int day = getDay(date, time);
		System.out.println("strDate: " + strDate);
		System.out.println("Date: " + date);
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
		switch(time.substring(0, 3))
		{
		case "Mon":week = 1;break;
		case "Tue":week = 2;break;
		case "Wed":week = 3;break;
		case "Thu":week = 4;break;
		case "Fri":week = 5;break;
		case "Sat":week = 6;break;
		case "Sun":week = 0;break;
		}
		int sub = week - current_day;
		return (sub + 7) % 7;
	}
	
	private static void generateCity(Map citys)
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

	public static String SEARCH_FLIGHT_URL = "https://secure.airtahitinui.com/OnlineBooking.aspx";
}
