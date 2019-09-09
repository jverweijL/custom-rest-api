package custom.rest.api.application;

import java.io.IOException;
import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Application;

import com.liferay.asset.kernel.model.AssetTag;
import com.liferay.asset.kernel.service.AssetTagLocalService;
import com.liferay.asset.kernel.service.AssetTagLocalServiceUtil;
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

		System.out.println(tags);

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
}