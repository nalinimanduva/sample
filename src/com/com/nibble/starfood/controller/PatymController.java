package com.nibble.starfood.controller;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.nibble.starfood.ServiceI.CustDetServiceI;
import com.nibble.starfood.hibernatemodel.PaytmTrans;

@Controller
public class PatymController {

    @Autowired
    CustDetServiceI cust;

    private static final Logger logger = LoggerFactory.getLogger(PatymController.class);

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "mobile/paytmtranscation", method = RequestMethod.POST)
    @ResponseBody
    public Object paytmmobileTestPost(@RequestParam(value = "CHANNEL_ID", required = false) String chnnlId, @RequestParam(value = "MID", required = false) String MID, @RequestParam(value = "CUST_ID", required = false) String custId, @RequestParam(value = "ORDER_ID", required = false) String orderId, @RequestParam(value = "EMAIL", required = false) String email, @RequestParam(value = "TXN_AMOUNT", required = false) String amount, @RequestParam(value = "MOBILE_NO", required = false) String mobile, @RequestParam(value = "THEME", required = false) String theme, @RequestParam(value = "WEBSITE", required = false) String website, @RequestParam(value = "INDUSTRY_TYPE_ID", required = false) String industry, @RequestParam(value = "REQUEST_TYPE", required = false) String REQUEST_TYPE, HttpServletRequest request, HttpServletResponse response) {
        final com.paytm.merchant.CheckSumServiceHelper checkSumServiceHelper = com.paytm.merchant.CheckSumServiceHelper.getCheckSumServiceHelper();
        final TreeMap<String, String> parameters = new TreeMap<String, String>();
        // Key provided by Paytm
        final String merchantKey = "p2!6#pZRmImML%BL";
        // Merchant ID (MID) provided by Paytm
        parameters.put("MID", MID);
        // Merchant�s order id
        parameters.put("ORDER_ID", orderId);
        // Customer ID registered with
        parameters.put("CUST_ID", custId);
        // merchant
        parameters.put("TXN_AMOUNT", amount);
        parameters.put("CHANNEL_ID", chnnlId);
        // Provided by Paytm
        parameters.put("INDUSTRY_TYPE_ID", industry);
        // Provided by Paytm
        parameters.put("WEBSITE", website);
        // user's mobile
        parameters.put("MOBILE_NO", mobile);
        parameters.put("EMAIL", email);
        // user's email
        parameters.put("REQUEST_TYPE", REQUEST_TYPE);
        parameters.put("THEME", theme);
        final JSONObject jo = new JSONObject();
        String checksum = null;
        logger.info("paytmtranscation>>>>>checking all parameters values...." + "\n merchant:" + merchantKey + "\n MID:" + MID + "\n ordid :" + orderId + "\n custID :" + custId + "\n amount :" + amount + "\n channelId :" + chnnlId + "\n industry :" + industry + "\n website :" + website + "\n mobile :" + mobile + "\n email :" + email + "\n requestType :" + REQUEST_TYPE + "\n theme :" + theme);//This Line is modified by Viofixer as a fix for Poor Logging Practice Use of a System Output Stream 
        try {
            logger.info(checkSumServiceHelper.genrateCheckSum(merchantKey, parameters));//This Line is modified by Viofixer as a fix for Poor Logging Practice Use of a System Output Stream 
            checksum = checkSumServiceHelper.genrateCheckSum(merchantKey, parameters);
            final String ch = checksum.replaceAll("\n", "");
            logger.info("CHECKSUMHASH:" + ch);//This Line is modified by Viofixer as a fix for Poor Logging Practice Use of a System Output Stream 
            jo.put("CHECKSUMHASH", ch.replaceAll("\\s+", ""));
            jo.put("payt_STATUS", 1);
            jo.put("ORDER_ID", orderId);
            jo.put("request_params", parameters.toString());
            return jo;
        } catch (final Exception e) {
            logger.info("Exception handling something in transaction url");//This Line is modified by Viofixer as a fix for Poor Logging Practice Use of a System Output Stream 
//            e.printStackTrace();  //This Line is commented by Viofixer as a fix for System Information Leak 
        }
        jo.put("CHECKSUMHASH", checksum.replaceAll("\\s+", ""));
        jo.put("payt_STATUS", 2);
        jo.put("ORDER_ID", orderId);
        logger.info("parameters are sending fine");//This Line is modified by Viofixer as a fix for Poor Logging Practice Use of a System Output Stream 
        // cust.savePaytmTrans(paytm);
        return jo;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "mobile/paytmtranscationios", method = RequestMethod.POST)
    @ResponseBody
    public Object paytmmobileTestPostIos(@RequestParam(value = "CHANNEL_ID", required = false) String chnnlId, @RequestParam(value = "MID", required = false) String MID, @RequestParam(value = "CUST_ID", required = false) String custId, @RequestParam(value = "ORDER_ID", required = false) String orderId, @RequestParam(value = "EMAIL", required = false) String email, @RequestParam(value = "TXN_AMOUNT", required = false) String amount, @RequestParam(value = "MOBILE_NO", required = false) String mobile, @RequestParam(value = "THEME", required = false) String theme, @RequestParam(value = "WEBSITE", required = false) String website, @RequestParam(value = "INDUSTRY_TYPE_ID", required = false) String industry, HttpServletRequest request, HttpServletResponse response) {
        final com.paytm.merchant.CheckSumServiceHelper checkSumServiceHelper = com.paytm.merchant.CheckSumServiceHelper.getCheckSumServiceHelper();
        logger.info("Check Generation from  IOS");
        final TreeMap<String, String> parameters = new TreeMap<String, String>();
        // Key provided by Paytm
        final String merchantKey = "p2!6#pZRmImML%BL";
        // Merchant ID (MID) provided by Paytm
        parameters.put("MID", MID);
        // Merchant�s order id
        parameters.put("ORDER_ID", orderId);
        // Customer ID registered with
        parameters.put("CUST_ID", custId);
        // merchant
        parameters.put("TXN_AMOUNT", amount);
        parameters.put("CHANNEL_ID", chnnlId);
        // Provided by Paytm
        parameters.put("INDUSTRY_TYPE_ID", industry);
        // Provided by Paytm
        parameters.put("WEBSITE", website);
        // user's mobile
        parameters.put("MOBILE_NO", mobile);
        parameters.put("EMAIL", email);
        parameters.put("THEME", theme);
        final JSONObject jo = new JSONObject();
        String checksum = null;
        logger.info("paytmtranscationios>>>>>checking all parameters values...." + "merchant" + merchantKey + "MID" + MID + "ordid" + orderId + "custID" + custId + "amount" + amount + "channelId" + chnnlId + "industry" + industry + "website" + website + "mobile" + mobile + "email" + email + "theme" + theme);//This Line is modified by Viofixer as a fix for Poor Logging Practice Use of a System Output Stream 
        try {
            logger.info(checkSumServiceHelper.genrateCheckSum(merchantKey, parameters));//This Line is modified by Viofixer as a fix for Poor Logging Practice Use of a System Output Stream 
            checksum = checkSumServiceHelper.genrateCheckSum(merchantKey, parameters);
            final String ch = checksum.replaceAll("\n", "");
            jo.put("CHECKSUMHASH", ch.replaceAll("\\s+", ""));
            jo.put("payt_STATUS", 1);
            jo.put("ORDER_ID", orderId);
            return jo;
        } catch (final Exception e) {
//            e.printStackTrace();  //This Line is commented by Viofixer as a fix for System Information Leak 
        }
        jo.put("CHECKSUMHASH", checksum.replaceAll("\\s+", ""));
        jo.put("payt_STATUS", 2);
        jo.put("ORDER_ID", orderId);
        // cust.savePaytmTrans(paytm);
        return jo;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "mobile/paytmmobile", method = RequestMethod.POST)
    public String paytmPost(@RequestParam(value = "SUBS_ID", required = false) String subId, @RequestParam(value = "MID", required = false) String MID, @RequestParam(value = "TXNID", required = false) String TXNID, @RequestParam(value = "ORDERID", required = false) String ORDERID, @RequestParam(value = "BANKTXNID", required = false) String BANKTXNID, @RequestParam(value = "TXNAMOUNT", required = false) String TXNAMOUNT, @RequestParam(value = "CURRENCY", required = false) String CURRENCY, @RequestParam(value = "STATUS", required = false) String STATUS, @RequestParam(value = "RESPCODE", required = false) String RESPCODE, @RequestParam(value = "RESPMSG", required = false) String RESPMSG, @RequestParam(value = "TXNDATE", required = false) String TXNDATE, @RequestParam(value = "GATEWAYNAME", required = false) String GATEWAYNAME, @RequestParam(value = "BANKNAME", required = false) String BANKNAME, @RequestParam(value = "PAYMENTMODE", required = false) String PAYMENTMODE, @RequestParam(value = "PROMO_CAMP_ID", required = false) String PROMO_CAMP_ID, @RequestParam(value = "PROMO_STATUS", required = false) String PROMO_STATUS, @RequestParam(value = "PROMO_RESPCODE", required = false) String PROMO_RESPCODE, @RequestParam(value = "CHECKSUMHASH", required = false) String CHECKSUMHASH, HashMap<Object, Object> model, HttpServletRequest request) throws Exception {
        logger.info("Check sum verification is Working Calling by Paytm");
        //This Line is modified by Viofixer as a fix for Poor Logging Practice Use of a System Output Stream 
        logger.info("paytmmobile>>>>>checking all parameters values....  " + "subId:  " + subId + "--------MID:  " + MID + "--------txnid:  " + TXNID + "--------ordid: " + ORDERID + "--------BANKTXNID: " + BANKTXNID + "--------TXNAMOUNT:  " + TXNAMOUNT + "--------CURRENCY: " + CURRENCY + "--------STATUS: " + STATUS + "--------RESPCODE: " + RESPCODE + "--------RESPMSG: " + RESPMSG + "--------TXNDATE: " + TXNDATE + "--------GATEWAYNAME: " + GATEWAYNAME + "--------BANKNAME: " + BANKNAME + "--------PAYMENTMODE: " + PAYMENTMODE + "--------PROMO_CAMP_ID: " + PROMO_CAMP_ID + "--------PROMO_STATUS: " + PROMO_STATUS + "--------PROMO_RESPCODE: " + PROMO_RESPCODE + "--------CHECKSUMHASH: " + CHECKSUMHASH);
        boolean isValidChecksum = false;
        final String merchantkey = "p2!6#pZRmImML%BL";
        final PaytmTrans paytm = new PaytmTrans();
        paytm.setSubscribtionId(subId);
        paytm.setMerchantId(MID);
        paytm.setTranscationId(TXNID);
        paytm.setApplicationId(ORDERID);
        paytm.setBankTranscationId(BANKTXNID);
        paytm.setAmount(TXNAMOUNT);
        paytm.setCurrency(CURRENCY);
        paytm.setStatus(STATUS);
        paytm.setRespcode(RESPCODE);
        paytm.setRespMsg(RESPMSG);
        paytm.setTransDate(TXNDATE);
        paytm.setGateWayName(GATEWAYNAME);
        paytm.setBankName(BANKNAME);
        paytm.setPaymentMode(PAYMENTMODE);
        paytm.setPromoStatus(PROMO_STATUS);
        paytm.setPromoId(PROMO_CAMP_ID);
        paytm.setPromoResponse(PROMO_RESPCODE);
        paytm.setChecksum(CHECKSUMHASH);
        //cust.savePaytmTrans(paytm);
        final com.paytm.merchant.CheckSumServiceHelper checkSumServiceHelper = com.paytm.merchant.CheckSumServiceHelper.getCheckSumServiceHelper();
        final TreeMap<String, String> parameters = new TreeMap<String, String>();
        // Merchant ID (MID) sent by Paytm pg
        parameters.put("MID", MID);
        // Merchant�s order id
        parameters.put("ORDERID", ORDERID);
        parameters.put("TXNAMOUNT", TXNAMOUNT);
        // sent by Paytm pg
        parameters.put("CURRENCY", CURRENCY);
        // sent by Paytm pg
        parameters.put("TXNID", TXNID);
        // Merchant ID (MID) sent by
        parameters.put("BANKTXNID", BANKTXNID);
        // Transaction id sent by Paytm pg
        parameters.put("STATUS", STATUS);
        // Merchant�s order id
        parameters.put("RESPCODE", RESPCODE);
        // Bank TXN id sent by Paytm pg
        parameters.put("RESPMSG", RESPMSG);
        parameters.put("TXNDATE", TXNDATE);
        // sent by Paytm pg
        parameters.put("GATEWAYNAME", GATEWAYNAME);
        // sent by Paytm pg
        parameters.put("BANKNAME", BANKNAME);
        parameters.put("PAYMENTMODE", PAYMENTMODE);
        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String paramName = parameterNames.nextElement();
            logger.info("Request Parameter name" + paramName);
            final String[] paramValues = request.getParameterValues(paramName);
            for (final String paramValue : paramValues) {
                logger.info("Request Parameter Value" + paramValue);
            }
        }
        final JSONObject jo = new JSONObject();
        try {
            logger.info("Check sum verification is Working");
            isValidChecksum = checkSumServiceHelper.verifycheckSum(merchantkey, parameters, CHECKSUMHASH);
            logger.info("Check sum verification result:" + isValidChecksum);
            if (isValidChecksum) {
                logger.info("Check sum is true");
                jo.put("TXNID", TXNID);
                jo.put("BANKTXNID", BANKTXNID);
                jo.put("ORDERID", ORDERID);
                jo.put("TXNAMOUNT", TXNAMOUNT);
                jo.put("STATUS", STATUS);
                jo.put("TXNTYPE", "");
                jo.put("CURRENCY", CURRENCY);
                jo.put("GATEWAYNAME", GATEWAYNAME);
                jo.put("RESPCODE", RESPCODE);
                jo.put("RESPMSG", RESPMSG);
                jo.put("BANKNAME", BANKNAME);
                jo.put("MID", MID);
                jo.put("PAYMENTMODE", PAYMENTMODE);
                jo.put("REFUNDAMT", "");
                jo.put("TXNDATE", TXNDATE);
                jo.put("IS_CHECKSUM_VALID", "Y");
                model.put("response", jo.toString().replace("\"", "&quot;"));
                return "paytmchecksumresponse";
            } else {
                logger.info("Check sum is false");
                jo.put("TXNID", TXNID);
                jo.put("BANKTXNID", BANKTXNID);
                jo.put("ORDERID", ORDERID);
                jo.put("TXNAMOUNT", TXNAMOUNT);
                jo.put("STATUS", STATUS);
                jo.put("TXNTYPE", "");
                jo.put("CURRENCY", CURRENCY);
                jo.put("GATEWAYNAME", GATEWAYNAME);
                jo.put("RESPCODE", RESPCODE);
                jo.put("RESPMSG", RESPMSG);
                jo.put("BANKNAME", BANKNAME);
                jo.put("MID", MID);
                jo.put("PAYMENTMODE", PAYMENTMODE);
                jo.put("REFUNDAMT", "");
                jo.put("TXNDATE", TXNDATE);
                jo.put("IS_CHECKSUM_VALID", "N");
                model.put("response", jo.toString().replace("\"", "&quot;"));
                return "paytmchecksumresponse";
            }
        } catch (final Exception e) {
            logger.info("Check sum is false then came to exception");
            jo.put("TXNID", TXNID);
            jo.put("BANKTXNID", BANKTXNID);
            jo.put("ORDERID", ORDERID);
            jo.put("TXNAMOUNT", TXNAMOUNT);
            jo.put("STATUS", STATUS);
            jo.put("TXNTYPE", "");
            jo.put("CURRENCY", CURRENCY);
            jo.put("GATEWAYNAME", GATEWAYNAME);
            jo.put("RESPCODE", RESPCODE);
            jo.put("RESPMSG", RESPMSG);
            jo.put("BANKNAME", BANKNAME);
            jo.put("MID", MID);
            jo.put("PAYMENTMODE", PAYMENTMODE);
            jo.put("REFUNDAMT", "");
            jo.put("TXNDATE", TXNDATE);
            jo.put("IS_CHECKSUM_VALID", "N");
            model.put("response", jo.toString().replace("\"", "&quot;"));
            logger.info("loging respose" + jo);
            return "paytmchecksumresponse";
        }
    }
}
