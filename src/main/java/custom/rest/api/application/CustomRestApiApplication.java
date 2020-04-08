package custom.rest.api.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.*;
import javax.ws.rs.core.Application;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetTag;
import com.liferay.asset.kernel.service.AssetCategoryLocalServiceUtil;
import com.liferay.asset.kernel.service.AssetTagLocalService;
import com.liferay.asset.kernel.service.AssetTagLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.OrderByComparatorFactoryUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author jverweij
 *
 * http://localhost:8080/o/custom
 *
 */
@Component(
	property = {
		"osgi.jaxrs.application.base=/custom",
		"osgi.jaxrs.name=Greetings.Rest",
			"liferay.auth.verifier=false",
			"liferay.oauth2=false"
	},
	service = Application.class
)
public class CustomRestApiApplication extends Application {

	public Set<Object> getSingletons() {
		return Collections.<Object>singleton(this);
	}

	@GET
	@Produces("text/plain")
	public String working() {
		return "It works!";
	}



	// http://localhost:8080/o/custom/adres?postcode=3438dc&huisnummer=3
	@GET
	@Path("/adres")
	@Produces("text/plain")
	public String address(
			@QueryParam("postcode") String zipcode,
			@QueryParam("huisnummer") String housenumber) {

		System.out.println("Trying to fetch the address info...");


		// build http request and assign multipart upload data
		HttpUriRequest request = RequestBuilder
				.get("http://geodata.nationaalgeoregister.nl/locatieserver/free?fq=postcode:" + zipcode.replaceAll("\\s","") + "&fq=huisnummer:" + housenumber.replaceAll("\\s",""))
				//.addHeader("X-Api-Key","chiImWTzBc1u3GK4xbnsJ7eVVXGT7YQz37cvLA73")
				.build();

		return getRequest(request);

	}

	@GET
	@Path("/countries")
	@Produces("text/plain")
	public String countries()
	{
		// GET https://restcountries.eu/rest/v2/all
		System.out.println("Trying to fetch the country info...");


		// build http request and assign multipart upload data
		HttpUriRequest request = RequestBuilder
				.get("https://restcountries.eu/rest/v2/all")
				.build();

		return getRequest(request);
	}

	//http://localhost:8080/o/custom/tags?groupId=20127
	@GET
	@Path("/tags")
	@Produces("text/plain")
	public String tags(@QueryParam("groupId") long groupId)
	{
		System.out.println("Trying to fetch the tags...");

		String result = "[";

		// get distinct values
		Set<String> temp = new HashSet<String>();

		List<AssetTag> tags = AssetTagLocalServiceUtil.getGroupTags(groupId);
		for (AssetTag tag : tags) {
			temp.add(tag.getName());
		}

		for (String tag : temp) {
			result += "\"" + tag +  "\",";
		}

		result = result.substring(0,result.lastIndexOf(',')) + "]";

		System.out.println(result);

		return result;
	}

	//http://localhost:8080/o/custom/categories?categoryId=43291
	@GET
	@Path("/categories")
	@Produces("text/plain")
	public String getCategories(@QueryParam("categoryId") List<Long> categoryId) {
		System.out.println("Trying to fetch the categories...");

		String result = "[";

		// get distinct values
		Set<String> temp = new HashSet<String>();

		List<AssetCategory> categories;
		for(Long catId : categoryId) {
			categories = AssetCategoryLocalServiceUtil.getChildCategories(catId);
			for (AssetCategory category : categories) {
				temp.add(category.getName());
			}
		}

		for (String category : temp) {
			result += "\"" + category +  "\",";
		}

		result = result.substring(0,result.lastIndexOf(',')) + "]";

		System.out.println(result);

		return result;
	}

	//http://localhost:8080/o/custom/categories?categoryId=43291&categoryId=43293
	@GET
	@Path("/vocabulary")
	@Produces("text/plain")
	public String getVocabulary(@QueryParam("vocabularyId") long vocabularyId) {
		System.out.println("Trying to fetch the vocabulary...");

		String result = "[";

		OrderByComparator comparator = OrderByComparatorFactoryUtil.create("AssetCategory", "name",true);
		List<AssetCategory> categories = AssetCategoryLocalServiceUtil.getVocabularyCategories(vocabularyId,0,999,comparator);

		// get distinct values
		Set<String> temp = new HashSet<String>();

		for (AssetCategory category : categories) {
			temp.add(getCategoryPath(category));
		}

		for (String category : temp) {
			result += "\"" + category +  "\",";
		}

		result = result.substring(0,result.lastIndexOf(',')) + "]";

		System.out.println(result);

		return result;
	}

	//recursive get fullpath
	private String getCategoryPath(AssetCategory category) {
		String result = "";

		if (category.isRootCategory()) {
			result = category.getName();
		}
		else {
			result += getCategoryPath(category.getParentCategory()) +  " > " + category.getName();
		}

		return result;
	}

	private String getRequest(HttpUriRequest request) {
		HttpClient client = HttpClientBuilder.create().build();

		HttpResponse response = null;
		try {
			response = client.execute(request);
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}


	//http://localhost:8080/o/custom/morning
	@GET
	@Path("/morning")
	@Produces("text/plain")
	public String hello() {
		return "Good morning!";
	}

	@GET
	@Path("/morning/{name}")
	@Produces("text/plain")
	public String morning(
			@PathParam("name") String name,
			@QueryParam("drink") String drink) {

		String greeting = "Good Morning " + name;

		if (drink != null) {
			greeting += ". Would you like some " + drink + "?";
		}

		return greeting;
	}

	// Example from https://stackoverflow.com/questions/14800597/get-system-uptime-in-java/14801764
	@GET
	@Path("/uptime")
	@Produces("text/plain")
	public static long getSystemUptime() throws Exception {
		long uptime = -1;
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")) {
			Process uptimeProc = Runtime.getRuntime().exec("net stats srv");
			BufferedReader in = new BufferedReader(new InputStreamReader(uptimeProc.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				if (line.startsWith("Statistics since")) {
					SimpleDateFormat format = new SimpleDateFormat("'Statistics since' MM/dd/yyyy hh:mm:ss a");
					Date boottime = format.parse(line);
					uptime = System.currentTimeMillis() - boottime.getTime();
					break;
				}
			}
		} else if (os.contains("mac") || os.contains("nix") || os.contains("nux") || os.contains("aix")) {
			Process uptimeProc = Runtime.getRuntime().exec("uptime");
			BufferedReader in = new BufferedReader(new InputStreamReader(uptimeProc.getInputStream()));
			String line = in.readLine();
			System.out.println ("Current uptime is: " + line);
			if (line != null) {
				Pattern parse = Pattern.compile("((\\d+) days,)? (\\d+):(\\d+)");
				Matcher matcher = parse.matcher(line);
				if (matcher.find()) {
					String _days = matcher.group(2);
					String _hours = matcher.group(3);
					String _minutes = matcher.group(4);
					int days = _days != null ? Integer.parseInt(_days) : 0;
					int hours = _hours != null ? Integer.parseInt(_hours) : 0;
					int minutes = _minutes != null ? Integer.parseInt(_minutes) : 0;
					uptime = (minutes * 60000) + (hours * 60000 * 60) + (days * 6000 * 60 * 24);
				}
			}
		}
		return uptime;
	}
}