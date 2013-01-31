<%@page import="java.util.List"%>
<%@page import="javax.persistence.EntityManager"%>
<%@page import="javax.persistence.EntityManagerFactory"%>
<%@page import="javax.persistence.Persistence"%>
<%@page import="javax.persistence.Query"%>
<%@page
	import="com.sap.core.odata.processor.ref.JPAReferenceServiceFactory"%>
<%@page import="com.sap.core.odata.processor.ref.util.DataGenerator"%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>

<style type="text/css">
body {
	font-family: "Arial";
}
</style>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Welcome to JPA implementation</title>
</head>

<body>

	<h1>SAP OData JPA library</h1>
	<hr />
	<table width="100%">
		<tr>
			<td width="90%"><h2>Reference Scenario</h2> [<a href="./image/model.jpg" target=_blank>Persistence Model</a>]</td>
			<td width="10%">
				<table cellspacing="0" cellpadding="0">
					<tr align="center">
						<td width="10%">
							<form name="form1" method="get">
								<input type="hidden" name="button" value="Generate"> <input
									type="submit" value="Generate Data" width="100%">
							</form>
						</td>
						<td width="10%">

							<form name="form2" method="get">
								<input type="hidden" name="button" value="Clean"> <input
									type="submit" value="   Clean Data  " width="100%">
							</form>
						</td>
					</tr>

					<%
						EntityManagerFactory entityManagerFactory = Persistence
								.createEntityManagerFactory("salesorderprocessing");
						EntityManager entityManager = entityManagerFactory
								.createEntityManager();
						DataGenerator dataGenerator = new DataGenerator(entityManager);

						Number result1 = null;
						Number existingCount = null;

						String msg = null;
						if (request.getParameter("button") != null) {
							if (request.getParameter("button").equalsIgnoreCase("Generate")) {
								Query q = entityManager
										.createQuery("SELECT COUNT(x) FROM SalesOrderHeader x");
								existingCount = (Number) q.getSingleResult();
								if (existingCount.intValue() == 0) { // Generate only if no data!
									dataGenerator.generate();
									result1 = (Number) q.getSingleResult();
									System.out
											.println("Data not existing earlier.... Generated number of Items - "
													+ result1);
									msg = result1 + " items generated ";

								} else {
									System.err
											.println("Data already existing.... No Item generated by Data Generator !!");
									msg = "Data exists. No Item generated !!";
								}
							} else { //Clean

								// Check if data already exists
								Query q = entityManager
										.createQuery("SELECT COUNT(x) FROM SalesOrderHeader x");
								Number result = (Number) q.getSingleResult();
								if (result.intValue() > 0) { // Generate only if no data!
									dataGenerator.clean();
									msg = "Data Cleaned. " + result + " items cleaned.";
								} else {
									msg = " Nothing to clean!!";
								}
							}
					%>
					<tr>
						<td colspan="2"><h5><%=(msg)%></h5></td>
					</tr>
					<%
						}
					%>

				</table>
			</td>
		</tr>
	</table>
	<hr>
	<h4>Metadata and Service Document</h4>
	<ul>
		<li><a href="SalesOrderProcessing.svc?_wadl" target="_blank">wadl</a></li>
		<li><a href="SalesOrderProcessing.svc/" target="_blank">service
				document</a></li>
		<li><a href="SalesOrderProcessing.svc/$metadata" target="_blank">metadata</a></li>
	</ul>

	<h4>System Query Options</h4>
	<table width=80% border="1" cellpadding="3" cellspacing="0">

		<tr>
			<th>OData Request</th>
			<th>JPQL Statement</th>
		</tr>

		<tr>
			<td width=40%><font size="3"><a
				href="SalesOrderProcessing.svc/SalesOrderHeaders" target="_blank">Simple
					Query</a></font></td>
			<td width=60%><code>SELECT E1 FROM SalesOrderHeader E1</code></td>
		</tr>
		<tr>
			<td width=40%><a
			href="SalesOrderProcessing.svc/SalesOrderHeaders?$filter=SoId eq 1"
			target="_blank">Query for Sales Order Id 1</a></td>
			<td width=60%><code>SELECT E1 FROM SalesOrderHeader E1 WHERE E1.soId = 1</code></td>
		</tr>
		

	</table>
	<ul>
		<li><a
			href="SalesOrderProcessing.svc/SalesOrderHeaders?$filter=BuyerId eq 2"
			target="_blank">Query for Buyer Id 2</a></li>
		<li><a
			href="SalesOrderProcessing.svc/SalesOrderHeaders?$filter=BuyerName eq 'buyerName_3'"
			target="_blank">Query for Buyer Name</a></li>
		<li><a
			href="SalesOrderProcessing.svc/SalesOrderHeaders?$filter=CurrencyCode eq 'Code_4'"
			target="_blank">Query for Currency Code</a></li>
		<li><a
			href="SalesOrderProcessing.svc/SalesOrderHeaders?$filter=DeliveryStatus eq false"
			target="_blank">Query for Delivery Status</a></li>
		<li><a
			href="SalesOrderProcessing.svc/SalesOrderHeaders?$filter=CreationDate eq SYSDATE"
			target="_blank">Query for Creation Date</a></li>
		<li><a
			href="SalesOrderProcessing.svc/SalesOrderHeaders?$filter=BuyerAddress/HouseNumber eq 7"
			target="_blank">Query for Member Expression:House Number</a></li>
		<li><a
			href="SalesOrderProcessing.svc/SalesOrderHeaders?$filter=BuyerAddress/StreetName eq 'Test_Street_Name_8'"
			target="_blank">Query for Member Expression:Street Name</a></li>
		<li><a
			href="SalesOrderProcessing.svc/SalesOrderHeaders?$filter=BuyerAddress/City eq 'Test_City_9'"
			target="_blank">Query for Member Expression:City</a></li>
		<li><a
			href="SalesOrderProcessing.svc/SalesOrderHeaders?$filter=BuyerAddress/Country eq 'Test_Country_10'"
			target="_blank">Query for Member Expression:Country</a></li>
		<li><a
			href="SalesOrderProcessing.svc/SalesOrderHeaders?$filter=BuyerId le 5 and BuyerAddress/Country eq 'Test_Country_3'"
			target="_blank">Query using AND operator</a></li>
		<li><a
			href="SalesOrderProcessing.svc/SalesOrderHeaders?$filter=BuyerId le 5 or SoId gt 5"
			target="_blank">Query using OR operator</a></li>
		<li><a
			href="SalesOrderProcessing.svc/SalesOrderHeaders?$orderby=BuyerId desc"
			target="_blank">ORDERBY System query option:Descending Order</a></li>
		<li><a
			href="SalesOrderProcessing.svc/SalesOrderHeaders?$orderby=SoId asc"
			target="_blank">ORDERBY System query option:Ascending Order</a></li>
		<li><a href="SalesOrderProcessing.svc/SalesOrderHeaders?$top=3"
			target="_blank">Top System query option</a></li>
		<li><a href="SalesOrderProcessing.svc/SalesOrderHeaders?$skip=2"
			target="_blank">Skip System query option</a></li>
		<li><a
			href="SalesOrderProcessing.svc/SalesOrderHeaders?$inlinecount=allpages"
			target="_blank">Inlinecount System query option</a></li>
		<li><a
			href="SalesOrderProcessing.svc/SalesOrderHeaders?$select=BuyerId,BuyerAddress/Country"
			target="_blank">Select System query option</a></li>
		<li><a
			href="SalesOrderProcessing.svc/SalesOrderHeaders?$skip=2&$top=2&$orderby=SoId"
			target="_blank">Mixed Query Example</a></li>
	</ul>

	<h4>Read Operation</h4>
	<ul>
		<li><a href="SalesOrderProcessing.svc/SalesOrderHeaders(1L)"
			target="_blank">Read operation</a></li>
		<li><a
			href="SalesOrderProcessing.svc/SalesOrderHeaders(3L)?$select=BuyerName,CurrencyCode"
			target="_blank">Read operation with Select system query option</a></li>
	</ul>

	<h4>Exception handling</h4>
	<ul>
		<li><a href="SalesOrderProcessing.svc/SalesOrderHeaderss"
			target="_blank">Exception Handling:Case 1</a></li>
		<li><a
			href="SalesOrderProcessing.svc/SalesOrderHeaders?$filter=BuyerAddress/City eq 'Test'"
			target="_blank">Exception Handling:Case 2</a></li>
		<li><a href="SalesOrderProcessing.svc/SalesOrderHeaders?$skip=12"
			target="_blank">Exception Handling:Case 3</a></li>
	</ul>

	<hr />

</body>
</html>