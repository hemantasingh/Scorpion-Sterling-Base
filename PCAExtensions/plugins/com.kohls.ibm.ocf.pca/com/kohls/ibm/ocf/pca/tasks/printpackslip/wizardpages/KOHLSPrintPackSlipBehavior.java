	
/*
 * Created on May 30,2012
 *
 */
package com.kohls.ibm.ocf.pca.tasks.printpackslip.wizardpages;

import javax.xml.xpath.XPathConstants;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Combo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.kohls.ibm.ocf.pca.rcp.KohlsApplicationInitializer;
import com.kohls.ibm.ocf.pca.tasks.printpackslip.actions.KOHLSPrintPackSlipPrintMultisAction;
import com.kohls.ibm.ocf.pca.util.KOHLSInputXMLUtils;
import com.kohls.ibm.ocf.pca.util.KOHLSPCAApiNames;
import com.kohls.ibm.ocf.pca.util.KOHLSPCAConstants;
import com.kohls.ibm.ocf.pca.util.KOHLSPCAUtils;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCSharedTaskOutput;
import com.yantra.yfc.rcp.YRCXPathUtils;
import com.yantra.yfc.rcp.YRCXmlUtils;

/**
 * @author admin
 *
 * Generated by RCP Tools
 */
 
public class KOHLSPrintPackSlipBehavior extends YRCBehavior {

	private static final String WIZARD_ID = "com.kohls.ibm.ocf.pca.tasks.printpackslip.wizards.KOHLSPrintPackSlipWizard";
	private static final String strDefaultPackPrinter = KOHLSPCAConstants.INI_PROPERTY_DEFAULT_PACK_PRINTER;
	/**
	 * Constructor for the behavior class. 
	 */
	 
    public KOHLSPrintPackSlipBehavior(Composite ownerComposite, String formId) {
        super(ownerComposite, formId);
       
    }
    
	/**
	 * This method initializes the behavior class.
	 */    
	public void init() {
		/*
		 * Pawan - Drop2 -Start - Property will be read from the KohlsApplicationIntializer
		 */
		//callGetDeviceList();
		setPrinterList();
		//Pawan - Drop2 -End
		setTotesPerCartModel(getNoOfTotes());
	    callAvailableCarts();
	}
	
	

	private void callAvailableCarts() {
		callApi(KOHLSPCAApiNames.API_GET_AVAILABLE_CARTS, 
				getInputForAvailableCarts(getNoOfTotes(), 
						KOHLSPCAUtils.getCurrentStore()));
		
	}

	private Document getInputForAvailableCarts(String noOfTotes, String currentStore) {
		Document docAvailableCartInput = YRCXmlUtils.createDocument(KOHLSPCAConstants.E_SHIPMENT);
		Element eAvailableCartInput = docAvailableCartInput.getDocumentElement();		
		eAvailableCartInput.setAttribute(KOHLSPCAConstants.A_SHIP_NODE,currentStore);	
		eAvailableCartInput.setAttribute(KOHLSPCAConstants.A_TOTES_PER_CART,noOfTotes);
		return docAvailableCartInput;
	}

	private void setTotesPerCartModel(String strNoOfTotes) {
		
		Element eTotesPerCart=YRCXmlUtils.createDocument(KOHLSPCAConstants.E_TOTE).getDocumentElement();
		eTotesPerCart.setAttribute(KOHLSPCAConstants.A_TOTES_PER_CART, strNoOfTotes);
		setModel("TotesPerCart",eTotesPerCart);
		
		
	}

	private void callGetDeviceList() {
		
		callApi(KOHLSPCAApiNames.API_GET_DEVICE_LIST, 
				getInputForDeviceList(KOHLSPCAConstants.V_PRINTER, 
						KOHLSPCAUtils.getCurrentStore()));
		
	}

	@Override
	public void handleApiCompletion(YRCApiContext ctx) {

		if (ctx.getInvokeAPIStatus() < 1) {
			YRCPlatformUI.trace("API exception in " + getFormId()
					+ " page, ApiName " + ctx.getApiName()
					+ ",Exception : ", ctx.getException());
		}else{
	
		
		if (ctx.getApiName().equals(KOHLSPCAApiNames.API_GET_ITEM_LIST)) {
			YRCPlatformUI.trace(
					"The command name is "+KOHLSPCAApiNames.API_GET_ITEM_LIST,
					null);
			
			Element eOutputGetItemListForOrder = ctx.getOutputXml().getDocumentElement();
			
			if(!YRCPlatformUI.isVoid(eOutputGetItemListForOrder)){
				
				Double dTotalItemList=YRCXmlUtils.getDoubleAttribute(eOutputGetItemListForOrder,KOHLSPCAConstants.A_TOTAL_ITEM_LIST);	
				
				if(dTotalItemList < 1){
					
				   addFieldInError("txtItemID", YRCPlatformUI.getString("PICK_SLIP_ERROR_INVALID_ITEM"));
				   getControl("txtItemID").setFocus();
					   	 
				}
				else{
					getControl("txtNoOfShipments").setFocus();
				}
				
				
			}
		
		}
		
		if (ctx.getApiName().equals(KOHLSPCAApiNames.API_GET_DEVICE_LIST)) {
			YRCPlatformUI.trace(
					"The command name is "+KOHLSPCAApiNames.API_GET_DEVICE_LIST,
					null);
			
			Element eOutputGetDeviceList= ctx.getOutputXml().getDocumentElement();
			
			if(!YRCPlatformUI.isVoid(eOutputGetDeviceList)){
				
				//String strPrinterID=System.getProperty(KOHLSPCAConstants.INI_PROPERTY_PRINTER_ID);
				//modified by Zubair 1Oct 2012
				String strPrinterID=System.getProperty(KOHLSPCAConstants.INI_PROPERTY_PICK_PRINTER_ID);
				
				
				if(!YRCPlatformUI.isVoid(strPrinterID) && 
						!YRCPlatformUI.equals(strPrinterID, "")){
				
				  defaultPrinterID(eOutputGetDeviceList, strPrinterID);
				  
				}
				
			     setModel("getDeviceList",eOutputGetDeviceList);
			     
			}
		
		}
		
		if (ctx.getApiName().equals(KOHLSPCAApiNames.API_KOHLS_PRINT_PACK_SLIP_SERVICE)) {
			YRCPlatformUI.trace(
					"The command name is "+KOHLSPCAApiNames.API_KOHLS_PRINT_PACK_SLIP_SERVICE,
					null);
			
			Element elePrintOuput= ctx.getOutputXml().getDocumentElement();
			
			//Disable Print Multis Button as soon as Print is fired, this button is enables only when refresh button is clicked
			
			disableField("btnPrintMultis");
			YRCPlatformUI.enableAction(KOHLSPrintPackSlipPrintMultisAction.ACTION_ID,false);
			
			
			
			if(!YRCPlatformUI.isVoid(elePrintOuput)){

				YRCPlatformUI.setMessage(YRCPlatformUI.getFormattedString(YRCPlatformUI.getString("MULTI_PICK_SLIP_PRINT_INFO"), new String[]{getFieldValue("cmbMPrinterID"),elePrintOuput.getAttribute(KOHLSPCAConstants.A_BATCH_ID)}));

			     setModel("PrintOutPut",elePrintOuput);
			     
			     
			}
		
		}
		
		if (ctx.getApiName().equals(KOHLSPCAApiNames.API_GET_AVAILABLE_CARTS)) {
			YRCPlatformUI.trace(
					"The command name is "+KOHLSPCAApiNames.API_GET_AVAILABLE_CARTS,
					null);
			
			Element eleAvailableCartsOutput= ctx.getOutputXml().getDocumentElement();
            //Start - Enable Print Multis Button as soon as Refresh button is fired
			
			enableField("btnPrintMultis");
			YRCPlatformUI.enableAction(KOHLSPrintPackSlipPrintMultisAction.ACTION_ID,true);
			YRCPlatformUI.setMessage("");
			
			//End
			
			
			if(!YRCPlatformUI.isVoid(eleAvailableCartsOutput)){

			     setModel("AvailableCartsOutput",eleAvailableCartsOutput);
			     
			     
			     
			     
			     
			}
		
		}
		
		}
		super.handleApiCompletion(ctx);
	}


	private void defaultPrinterID(Element outputGetDeviceList, String strPrinterID ) {

		NodeList nOutputPrinterList = outputGetDeviceList
				.getElementsByTagName(KOHLSPCAConstants.E_DEVICE);

		if (!YRCPlatformUI.isVoid(nOutputPrinterList)
				&& nOutputPrinterList.getLength() > 1) {

		KOHLSPCAUtils.rearrangePrinterID(outputGetDeviceList, strPrinterID);

		}
		
	}

	private void rearrangePrinterID(Element outputGetDeviceList, String strPrinterID) {
		Element eDevice = (Element) YRCXPathUtils.evaluate(outputGetDeviceList,
				"/Devices/Device[@DeviceId='"
						+ strPrinterID + "']", XPathConstants.NODE);

		if (!YRCPlatformUI.isVoid(eDevice)) {

			// This code to show the New Container Number for auto generated
			// container
			// Walk backwards until we find the first sibling in the parent
			//
			Node previousNode = eDevice.getPreviousSibling();
			while (previousNode.getPreviousSibling() != null
					&& !(previousNode == eDevice)) {
				previousNode = previousNode.getPreviousSibling();
			}
			if (previousNode.getPreviousSibling() == null) {
				try {

					if (!previousNode.isSameNode(eDevice)) {
						eDevice = (Element) outputGetDeviceList
								.removeChild(eDevice);
						outputGetDeviceList.insertBefore(eDevice, previousNode);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} else {
			YRCPlatformUI.showError(YRCPlatformUI.getString("ERROR"),
					YRCPlatformUI.getString("ERROR_DEFAULTING_PRINTER_ID"));
		}

		
	}

	public void validateItemEntered() {
		if (YRCPlatformUI.equals(getFieldValue("txtItemID").trim(),"")){		
			return;		
		}
		
		Element ePrintSinglePackShipment=getTargetModel("PrintSinglePackShipment");
	
		if (!YRCPlatformUI.isVoid(ePrintSinglePackShipment.getAttribute(KOHLSPCAConstants.A_ITEM_ID)) ) {
			
			Element eleItemInput=YRCXmlUtils.createDocument(KOHLSPCAConstants.E_ITEM).getDocumentElement();
			eleItemInput.setAttribute(KOHLSPCAConstants.A_ITEM_ID, ePrintSinglePackShipment.getAttribute(KOHLSPCAConstants.A_ITEM_ID));
			
			callApi(KOHLSPCAApiNames.API_GET_ITEM_LIST,
					KOHLSInputXMLUtils.getInputForItemList(eleItemInput.getAttribute(KOHLSPCAConstants.A_ITEM_ID),
							KOHLSPCAUtils.getEnterpriseCodeForStoreUser(), KOHLSPCAConstants.V_PROD));
		} 
		
		
	}
	
	void callApi(String name, Document inputXml) {
		YRCApiContext context = new YRCApiContext();		
		context.setApiName(name);
		context.setFormId(WIZARD_ID);
	    context.setInputXml(inputXml);
	    callApi(context);
	}

	public void searchItem() {
		
		Element ePrintSinglePackShipment=getTargetModel("PrintSinglePackShipment");
		
		Element eleItemInput=YRCXmlUtils.createDocument(KOHLSPCAConstants.E_ITEM).getDocumentElement();
		eleItemInput.setAttribute(KOHLSPCAConstants.A_ITEM_ID, ePrintSinglePackShipment.getAttribute(KOHLSPCAConstants.A_ITEM_ID));
			
		if (!YRCPlatformUI.isVoid(eleItemInput) ) {
			
			YRCSharedTaskOutput stItemSearchOutput=YRCPlatformUI.launchSharedTask(KOHLSPCAConstants.SHARED_TASK_ITEM_SEARCH_LOOKUP, eleItemInput);
			
			Element eItemSearchOutput=stItemSearchOutput.getOutput();
			
			if(!YRCPlatformUI.isVoid(eItemSearchOutput)){
				
				String strItemID=eItemSearchOutput.getAttribute(KOHLSPCAConstants.A_ITEM_ID);
				
				setFieldValue("txtItemID", strItemID);
				
				getControl("txtNoOfShipments").setFocus();
				
				
			}
			
			
		} 
		
		return;
		
	}
	
	private  Document getInputForDeviceList(String strDeviceType, String strOrganizationCode){
		
		Document docDevice = YRCXmlUtils.createDocument(KOHLSPCAConstants.E_DEVICE);
		Element eDeviceType = docDevice.getDocumentElement();		
		eDeviceType.setAttribute(KOHLSPCAConstants.A_DEVICE_TYPE,strDeviceType);	
		eDeviceType.setAttribute(KOHLSPCAConstants.A_ORGANIZATION_CODE,strOrganizationCode);
		return docDevice;
		
	}


	public String getNoOfTotes() {
		String strTotes="";
		
		//if(!YRCPlatformUI.isVoid(System.getProperty(KOHLSPCAConstants.INI_PROPERTY_TOTES))){
				//strTotes= System.getProperty(KOHLSPCAConstants.INI_PROPERTY_TOTES);
			strTotes = KohlsApplicationInitializer.getTerminalPropertyForUISession(KOHLSPCAConstants.INI_SHIPMENT_PER_TOTES);
		//}
	
	     return strTotes;
	}

	public void singleprint() {
		
		Element eSinglePrintInput=getTargetModel("PrintSinglePackShipment");
		
		if(isValidSinglePrintInput(eSinglePrintInput)){
			
			eSinglePrintInput.setAttribute(KOHLSPCAConstants.A_PRINT_TYPE, KOHLSPCAConstants.V_PRINT_TYPE_SINGLE);
			eSinglePrintInput.setAttribute(KOHLSPCAConstants.A_SHIP_NODE, KOHLSPCAUtils.getCurrentStore());
			
			YRCPlatformUI.setMessage(YRCPlatformUI.getFormattedString(YRCPlatformUI.getString("PICK_SLIP_PRINT_INFO"), new String[]{getFieldValue("cmbPrinterID")}));
			
			callApi(KOHLSPCAApiNames.API_KOHLS_PRINT_PACK_SLIP_SERVICE, eSinglePrintInput.getOwnerDocument());
			
		}
		
	}

	private boolean isValidSinglePrintInput(Element eSinglePrintInput) {
		
		boolean flag=true;

        if(!YRCPlatformUI.isVoid(eSinglePrintInput)){
        	
        	String strItemID=eSinglePrintInput.getAttribute(KOHLSPCAConstants.A_ITEM_ID);
        	
        	if(YRCPlatformUI.equals(strItemID, "")){
        		
        		YRCPlatformUI.showError(YRCPlatformUI.getString("Print_Aborted"), YRCPlatformUI.getString("PICK_SLIP_ERROR_INVALID_ITEM"));
        		getControl("txtItemID").setFocus();
        		return false;
        	}
        	
            String strTotalShipments=eSinglePrintInput.getAttribute(KOHLSPCAConstants.A_TOTAL_SHIPMENTS);
        	
        	if(YRCPlatformUI.isVoid(strTotalShipments) || YRCPlatformUI.equals(strTotalShipments, "") || !checkIfNumber(strTotalShipments)){
        		
        		YRCPlatformUI.showError(YRCPlatformUI.getString("Print_Aborted"), YRCPlatformUI.getString("PICK_SLIP_ERROR_INVALID_TOTAL_SHIPMENT"));
        		getControl("txtNoOfShipments").setFocus();
        		return false;
        	
        	}
        	
        	String strPrinterID=eSinglePrintInput.getAttribute(KOHLSPCAConstants.A_PRINTER_ID);
           	
           	if(YRCPlatformUI.isVoid(strPrinterID) || YRCPlatformUI.equals(strPrinterID, "")){
           		
           		YRCPlatformUI.showError(YRCPlatformUI.getString("Print_Aborted"), YRCPlatformUI.getString("PICK_SLIP_ERROR_INVALID_PRINTER_ID"));
           		getControl("cmbPrinterID").setFocus();
           		return false;
           	
           	}
        	
        	
        }
		
		return flag;
	}

	
	public void multiprint() {
        
		Element eMultiPrintInput=getTargetModel("PrintMultiPackShipment");
		
		Element eTotesPerCart=getModel("TotesPerCart");
		
		if(!YRCPlatformUI.isVoid(eTotesPerCart)){
		
		eMultiPrintInput.setAttribute(KOHLSPCAConstants.A_TOTES_PER_CART,eTotesPerCart.getAttribute(KOHLSPCAConstants.A_TOTES_PER_CART));
		}
		else{
			eMultiPrintInput.setAttribute(KOHLSPCAConstants.A_TOTES_PER_CART,KOHLSPCAConstants.V_BLANK);
		}
	
		if(isValidMultiPrintInput(eMultiPrintInput)){
			
			eMultiPrintInput.setAttribute(KOHLSPCAConstants.A_PRINT_TYPE, KOHLSPCAConstants.V_PRINT_TYPE_MULTI);
			eMultiPrintInput.setAttribute(KOHLSPCAConstants.A_SHIP_NODE, KOHLSPCAUtils.getCurrentStore());
			
			//YRCPlatformUI.setMessage(YRCPlatformUI.getFormattedString(YRCPlatformUI.getString("PICK_SLIP_PRINT_INFO"), new String[]{getFieldValue("cmbMPrinterID")}));
			
			callApi(KOHLSPCAApiNames.API_KOHLS_PRINT_PACK_SLIP_SERVICE, eMultiPrintInput.getOwnerDocument());
			
		}
		
	}
	
	 private boolean isValidMultiPrintInput(Element eMultiPrintInput) {
		 
			
			boolean flag=true;

	        if(!YRCPlatformUI.isVoid(eMultiPrintInput)){
	        	
	        	String strTotesPerCart=eMultiPrintInput.getAttribute(KOHLSPCAConstants.A_TOTES_PER_CART);
	         	
	         	if(YRCPlatformUI.isVoid(strTotesPerCart) || YRCPlatformUI.equals(strTotesPerCart, "") || !checkIfNumber(strTotesPerCart)){
	         		
	         		YRCPlatformUI.showError(YRCPlatformUI.getString("Print_Aborted"), YRCPlatformUI.getString("PICK_SLIP_ERROR_INVALID_TOTES_PER_CART"));
	         		getControl("lblTotesPerCart").setFocus();
	         		return false;
	         	
	         	}	
	         	
	         	  String strTotalCarts=eMultiPrintInput.getAttribute(KOHLSPCAConstants.A_TOTAL_CARTS);
		        	
		        	if(YRCPlatformUI.isVoid(strTotalCarts) || YRCPlatformUI.equals(strTotalCarts, "") || !checkIfNumber(strTotalCarts)){
		        		
		        		YRCPlatformUI.showError(YRCPlatformUI.getString("Print_Aborted"), YRCPlatformUI.getString("PICK_SLIP_ERROR_INVALID_TOTAL_CARTS"));
		        		getControl("txtNoOfCarts").setFocus();
		        		return false;
		        	
		        	}
		        	else {
		        		
		        		if(!checkIfNumber(YRCPlatformUI.getString("MAX_NUMBER_OF_CARTS").trim())){
		        			YRCPlatformUI.showError(YRCPlatformUI.getString("Error"), YRCPlatformUI.getString("MSG_MAX_CART_NOT_A_NUMBER_ERROR"));
		        			return false;
		        		}
		        		
		        		else {
		        			if(!checkMaxCarts(strTotalCarts)){
		        				YRCPlatformUI.showError(YRCPlatformUI.getString("Error"), YRCPlatformUI.getFormattedString
		        						(YRCPlatformUI.getString("MSG_CART_INPUT_MORE_THAN_MAX_CART"),new String[]{YRCPlatformUI.getString("MAX_NUMBER_OF_CARTS")}));
		        				return false;
		        			}
		        			
		        		}
		        	}
	        	
	        	String strPrinterID=eMultiPrintInput.getAttribute(KOHLSPCAConstants.A_PRINTER_ID);
	           	
	           	if(YRCPlatformUI.isVoid(strPrinterID) || YRCPlatformUI.equals(strPrinterID, "")){
	           		
	           		YRCPlatformUI.showError(YRCPlatformUI.getString("Print_Aborted"), YRCPlatformUI.getString("PICK_SLIP_ERROR_INVALID_PRINTER_ID"));
	           		getControl("cmbMPrinterID").setFocus();
	           		return false;
	           	
	           	}
	        	
	        	
	        }
			
			return flag;
		
	}

	private boolean checkMaxCarts(String strTotalCarts) {
		
		try {

	           int i= Integer.parseInt(strTotalCarts);
	           int max=Integer.parseInt(YRCPlatformUI.getString("MAX_NUMBER_OF_CARTS").trim());
	           
	           Element elePrintInput=getTargetModel("PrintMultiPackShipment");;
	           
	           int noOfAvailCarts=0;
	           
	           
	           if (!YRCPlatformUI.isVoid(elePrintInput)) {

				String strShipmentType = elePrintInput
						.getAttribute(KOHLSPCAConstants.A_SHIPMENT_TYPE);
				Element eleAvailableCartsOutput = getModel("AvailableCartsOutput");

				if (YRCPlatformUI.equals(strShipmentType,
						KOHLSPCAConstants.V_MULTI_REGULAR)) {

					noOfAvailCarts = YRCXmlUtils.getIntAttribute(
							eleAvailableCartsOutput,
							KOHLSPCAConstants.V_MULTIREGULAR);

				}

				if (YRCPlatformUI.equals(strShipmentType,
						KOHLSPCAConstants.V_MULTI_GIFT)) {

					noOfAvailCarts = YRCXmlUtils.getIntAttribute(
							eleAvailableCartsOutput,
							KOHLSPCAConstants.V_MULTIGIFT);

				}

				if (YRCPlatformUI.equals(strShipmentType,
						KOHLSPCAConstants.V_MULTI_PRIORITY)) {

					noOfAvailCarts = YRCXmlUtils.getIntAttribute(
							eleAvailableCartsOutput,
							KOHLSPCAConstants.V_MULTIPRIORITY);

				}
	        	   
	        	   
	           }
	           
	           //Take minimun of available carts to print for specific shipmentType and Max of carts that can be prints
	           
	           max=Math.min(max, noOfAvailCarts);
	           
	           if(i > max) return false ;
	        
	        } catch (NumberFormatException ex) {
	        	
	            return false;
	        }
	        
	        return true;
	}
	

	/**
     * Validates if input String is a number
     */
    public boolean checkIfNumber(String in) {
        
        try {

           int i= Integer.parseInt(in);
           
           if(i < 1) return false ;
        
        } catch (NumberFormatException ex) {
            return false;
        }
        
        return true;
    }
    
    protected boolean validateShipmentNo(String strTotalShipments){
    	
    	boolean valid=true;
    	
    	if(!YRCPlatformUI.equals(strTotalShipments, "")){
    	if(!checkIfNumber(strTotalShipments)){
    		addFieldInError("txtNoOfShipments", YRCPlatformUI.getString("PICK_SLIP_ERROR_INVALID_TOTAL_SHIPMENT"));
    		YRCPlatformUI.showError(YRCPlatformUI.getString("ERROR"), YRCPlatformUI.getString("PICK_SLIP_ERROR_INVALID_TOTAL_SHIPMENT"));
    		setFieldValue("txtNoOfShipments", "");
    		getControl("txtNoOfShipments").setFocus();
    		valid=false;
    		
    	}
    	
    }
    	
    	return valid;
    }

	public void doRefresh() {
		this.callAvailableCarts();
		
	}
	
		/*
	 * Pawan - Drop2 -Start - Property will be read from the KohlsApplicationIntializer
	 * and will be cached for the UI Session
	 */
	/*
	 * Will get the list of the Printers configured on the terminal from the 
	 * KohlsApplicationInitializer and will set the model "getDeviceList" on
	 * screen.
	 * Before Setting the model on the screen - the first printer in the model
	 * will be the Default Printer to be used on this Screen.
	 * The value of the Default Printer to be used, will be picked from the 
	 * KohlsApplicationInitializer.
	 */
	public void setPrinterList(){
		String strDefaultPackPrinterID = KohlsApplicationInitializer.getTerminalPropertyForUISession(strDefaultPackPrinter);
		KOHLSPCAUtils.rearrangePrinterID(KohlsApplicationInitializer.elePrinterDevices, strDefaultPackPrinterID);
		setModel("getDeviceList",KohlsApplicationInitializer.elePrinterDevices,true);		
	}
	
	/*
	 * If SIM User selects a different printer on the UI,
	 * For the given session - that will be treated as the
	 * Default Printer.
	 */
	 public void handleComboBoxSelection(Combo cmbPrinterList){
	    	KohlsApplicationInitializer.modifyTerminalPropertyForUISession(strDefaultPackPrinter,cmbPrinterList.getText());
	 }
	//Pawan - Drop2 - End

}