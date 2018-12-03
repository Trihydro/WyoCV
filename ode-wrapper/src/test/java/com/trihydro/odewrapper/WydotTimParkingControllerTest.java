package com.trihydro.odewrapper;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimType;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.TimRsuService;
import com.trihydro.library.service.TimService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;
import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimIncident;
import com.trihydro.odewrapper.model.WydotTimList;
import com.trihydro.odewrapper.model.WydotTimParking;
import com.trihydro.odewrapper.model.WydotTravelerInputData;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import us.dot.its.jpo.ode.model.OdeLogMetadata;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@WebAppConfiguration
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class WydotTimParkingControllerTest {

	@Autowired
	private WebApplicationContext wac;
	private MockMvc mockMvc;
	private static final String CONTENT_TYPE = "application/json;charset=UTF-8";

	@Before
	public void setup() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	public static Long getTimTypeId() throws Exception {
		List<TimType> timTypes = TimTypeService.selectAll();

		TimType timType = timTypes.stream().filter(x -> x.getType().equals("P")).findFirst().orElse(null);

		Long timTypeId = timType.getTimTypeId();

		return timTypeId;
	}

	@Test
	public void givenWac_whenServletContext_thenItProvidesGreetController() {
		ServletContext servletContext = wac.getServletContext();

		Assert.assertNotNull(servletContext);
		Assert.assertTrue(servletContext instanceof MockServletContext);
		Assert.assertNotNull(wac.getBean("wydotTimParkingController"));
	}

	@Test
	public void testCreateParkingTim_oneDirection_success() throws Exception {

		String parkingJson = "{\"timParkingList\": [{ \"mileMarker\": 360, \"route\": \"I-80\", \"direction\": \"westbound\", \"availability\": 4103, \"clientId\": \"Parking49251\" }]}";

		ResultActions resultActions = this.mockMvc
				.perform(MockMvcRequestBuilders.post("/parking-tim").contentType(MediaType.APPLICATION_JSON)
						.content(parkingJson))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].clientId").value("Parking49251"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].route").value("I-80"));

		MvcResult mvcResult = resultActions.andReturn();
		String result = mvcResult.getResponse().getContentAsString();
		System.out.println(result);
	}

	@Test
	public void testCreateParkingTimTim_oneDirection_NoMileposts() throws Exception {

		String incidentJson = "{\"timParkingList\": [{ \"mileMarker\": 360, \"route\": \"I-70\", \"direction\": \"westbound\", \"availability\": 4103, \"clientId\": \"Parking49251\" }]}";

		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/parking-tim").contentType(MediaType.APPLICATION_JSON)
						.content(incidentJson))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("route not supported"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].route").value("I-70"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"));
	}

	@Test
	public void testCreateParkingTim_oneDirection_NoItisCodes() throws Exception {

		String incidentJson = "{\"timParkingList\": [{ \"mileMarker\": 360, \"route\": \"I-80\", \"direction\": \"westbound\", \"availability\": 345678, \"clientId\": \"Parking49251\" }]}";

		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/parking-tim").contentType(MediaType.APPLICATION_JSON)
						.content(incidentJson))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].clientId").value("Parking49251"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].route").value("I-80"));
	}

	@Test
	public void testUpdateParkingTim_oneDirection_success() throws Exception {

		String incidentJson = "{\"timParkingList\": [{ \"mileMarker\": 360, \"route\": \"I-80\", \"direction\": \"westbound\", \"availability\": 4103, \"clientId\": \"Parking49251\" }]}";

		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/parking-tim").contentType(MediaType.APPLICATION_JSON)
						.content(incidentJson))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].resultMessages[0]").value("success"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].clientId").value("Parking49251"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].route").value("I-80"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"));
	}

	@Test
	public void testDeleteParkingTimsByClientId() throws Exception {

		makeTims();

		List<ActiveTim> activeTimsBeforeDelete = ActiveTimService.getActiveTimsByClientId("Parking49251",
				getTimTypeId(), null);
		assertEquals(1, activeTimsBeforeDelete.size());

		this.mockMvc.perform(MockMvcRequestBuilders.delete("/parking-tim/Parking49251"))
				.andExpect(MockMvcResultMatchers.status().isOk());

		List<ActiveTim> activeTimsAfterDelete = ActiveTimService.getActiveTimsByClientId("Parking49251", getTimTypeId(),
				null);
		assertEquals(0, activeTimsAfterDelete.size());
	}

	@Test
	public void testGetParkingTims() throws Exception {

		makeTims();

		ResultActions resultActions = this.mockMvc.perform(MockMvcRequestBuilders.get("/parking-tim"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(CONTENT_TYPE))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"));

		MvcResult mvcResult = resultActions.andReturn();
		String result = mvcResult.getResponse().getContentAsString();
		System.out.println(result);
	}

	@Test
	public void testGetParkingTimsByClientId() throws Exception {

		makeTims();

		ResultActions resultActions = this.mockMvc.perform(MockMvcRequestBuilders.get("/parking-tim/Parking49251"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(CONTENT_TYPE));
		// .andExpect(MockMvcResultMatchers.jsonPath("$[0].direction").value("westbound"));

		MvcResult mvcResult = resultActions.andReturn();
		String result = mvcResult.getResponse().getContentAsString();
		System.out.println(result);
	}

	private void makeTims() {

		List<WydotTimParking> parkingList = new ArrayList<WydotTimParking>();
		WydotTimParking wydotTim = new WydotTimParking();

		wydotTim.setMileMarker(370.0);
		wydotTim.setRoute("I-80");
		wydotTim.setClientId("Parking49251");
		wydotTim.setDirection("westbound");
		wydotTim.setAvailability(4103);
		wydotTim.setFromRm(370.0);
		wydotTim.setToRm(380.0);

		parkingList.add(wydotTim);

		WydotTravelerInputData wydotTravelerInputData = CreateBaseTimUtil.buildTim(wydotTim, "westbound", "80");

		OdeLogMetadata odeTimMetadata = new OdeLogMetadata();
		odeTimMetadata.setOdeReceivedAt(null);

		Long timId = TimService.insertTim(odeTimMetadata, wydotTravelerInputData.getTim());

		TimRsuService.insertTimRsu(timId, 1, 1);

		ActiveTim activeTim = new ActiveTim();
		activeTim.setTimId(timId);
		activeTim.setClientId(wydotTim.getClientId());
		activeTim.setDirection("westbound");
		activeTim.setMilepostStart(wydotTim.getFromRm());
		activeTim.setMilepostStop(wydotTim.getToRm());
		activeTim.setStartDateTime(wydotTravelerInputData.getTim().getDataframes()[0].getStartDateTime());
		activeTim.setRoute(wydotTim.getRoute());
		try {
			activeTim.setTimTypeId(getTimTypeId());
		} catch (Exception e) {
			e.printStackTrace();
		}

		ActiveTimService.insertActiveTim(activeTim);
	}

}
