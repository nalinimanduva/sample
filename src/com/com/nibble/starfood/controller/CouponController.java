package com.nibble.starfood.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.nibble.starfood.ServiceI.CouponService;
import com.nibble.starfood.hibernatemodel.Cupn;
import com.nibble.starfood.webservices.model.ApplyCoupon;

@Controller
public class CouponController {

    @Autowired
    CouponService cust;

    /*@RequestMapping(value = "/mobile/applycoupon", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> applyCoupon(
			@RequestBody final ApplyCoupon cupn) {
		final Map<String, Object> coupondetails = new HashMap<String, Object>();

		List<Cupn> couponCodeDetails = cust.getCouponCodeDetails(cupn);

		for (Cupn cupndet : couponCodeDetails) {
			Date todayDate = getIndianTimezoneDatewithTime();
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String startDateInString = cupndet.getCupnStrtDt() ;
			String endDateInString=cupndet.getCupnEndDt();
			Date couponStartDate=null;
			Date couponEndDate=null;
			try {
				couponStartDate = formatter.parse(startDateInString);
				couponEndDate=formatter.parse(endDateInString);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			System.out.println("stardate>>>" + couponStartDate);
			System.out.println("enddate>>>" + couponEndDate);
			System.out.println("todayDate>>>" + todayDate);
			Long couponUsedCount = cust.getCouponUsedCount(cupn);
			// Cupn validCoupons=cust.getValidCoupons(cupndet, todayDate);

			Calendar cal = Calendar.getInstance();
			cal.setTime(couponEndDate);
			cal.add(Calendar.DATE, 1); // minus number would decrement the days
			couponEndDate = cal.getTime();

			if (Integer.parseInt(cupndet.getCupnMinAmnt()) <= cupn.getTotalAmount()) {
				if ((todayDate.after(couponStartDate)) && (todayDate.before(couponEndDate))) {
					if (cupndet.getCupnCatgId() == 1) {
						if (cupndet.getCustDetId() == cupn.getCustomerId()) {
							if (Integer.parseInt(cupndet.getUnltdCupnF()) == 1) {
								coupondetails.put("status","success for specific user");
								coupondetails.put("maxCouponAmount",cupndet.getMaxCupnAmount());
							} else {
								if (Integer.parseInt(cupndet.getCupnUsedCnt()) < couponUsedCount) {
									coupondetails.put("status","success for specific user with Coupon count");
									coupondetails.put("maxCouponAmount",cupndet.getMaxCupnAmount());
								} else {
									coupondetails
											.put("status","failure for specifuser with Coupon count");
								}
							}
						} else {
							coupondetails.put("status", "coupon is not applicable");
						}
					} else {
						coupondetails.put("status", "success");
						coupondetails.put("maxCouponAmount",cupndet.getMaxCupnAmount());
					}
				} else {
					coupondetails.put("status", "your coupon code has expired");
				}
			} else {
				coupondetails
						.put("status","your coupon not applicable with respect to min amount");
			}
		}

		coupondetails.put("cupnDetails", couponCodeDetails);
		return coupondetails;
	}*/
    @RequestMapping(value = "/mobile/applycoupon", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> applyCoupon(@RequestBody final ApplyCoupon cupn) {
        final Map<String, Object> coupondetails = new  HashMap<String, Object>();
        final Map<String, Object> map = new  HashMap<String, Object>();
        final List<Map<String, Object>> couponDet = new  ArrayList<Map<String, Object>>();
        List<Cupn> couponCodeDetails = cust.getCouponCodeDetails(cupn);
        System.out.println("couponSize: " + couponCodeDetails.size());
        if (couponCodeDetails.size() > 0) {
            for (Cupn cupndet : couponCodeDetails) {
                Date todayDate = getIndianTimezoneDatewithTime();
                SimpleDateFormat formatter = new  SimpleDateFormat("yyyy-MM-dd");
                String startDateInString = cupndet.getCupnStrtDt();
                String endDateInString = cupndet.getCupnEndDt();
                Date couponStartDate = null;
                Date couponEndDate = null;
                try {
                    couponStartDate = formatter.parse(startDateInString);
                    couponEndDate = formatter.parse(endDateInString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                System.out.println("stardate>>>" + couponStartDate);
                System.out.println("enddate>>>" + couponEndDate);
                System.out.println("todayDate>>>" + todayDate);
                Long couponUsedCount = cust.getCouponUsedCount(cupndet.getId(), cupn);
                Calendar cal = Calendar.getInstance();
                cal.setTime(couponEndDate);
                // minus number would decrement the days
                cal.add(Calendar.DATE, 1);
                couponEndDate = cal.getTime();
                if (Integer.parseInt(cupndet.getCupnMinAmnt()) <= cupn.getTotalAmount()) {
                    if ((todayDate.after(couponStartDate)) && (todayDate.before(couponEndDate))) {
                        if (cupndet.getCupnCatgId() == 1) {
                            if (cupndet.getCustDetId() == cupn.getCustomerId()) {
                                if (Integer.parseInt(cupndet.getUnltdCupnF()) == 1) {
                                    coupondetails.put("status", "success for specific user");
                                    coupondetails.put("couponPercentage", cupndet.getCupnPrcnt());
                                    coupondetails.put("couponAmount", cupndet.getCupnAmount());
                                    /*coupondetails.put("maxCouponAmount",cupndet.getMaxCupnAmount());
								coupondetails.put("minCouponAmount",cupndet.getCupnMinAmnt());
								coupondetails.put("couponAmountPercentage",cupndet.getCupnPrcnt());*/
                                    float couponAmnt = Float.parseFloat(cupndet.getCupnAmount());
                                    float couponPercent = (cupn.getTotalAmount() / 100) * (Float.parseFloat(cupndet.getCupnPrcnt()));
                                    float cupnMaxAmount = cupndet.getMaxCupnAmount();
                                    float remainingTotalAmnt = 0;
                                    if (couponAmnt == 0) {
                                        if (cupnMaxAmount >= couponPercent) {
                                            remainingTotalAmnt = cupn.getTotalAmount() - couponPercent;
                                            coupondetails.put("appliedCouponAmount", couponPercent);
                                        } else if (cupnMaxAmount < couponPercent) {
                                            remainingTotalAmnt = cupn.getTotalAmount() - cupnMaxAmount;
                                            coupondetails.put("appliedCouponAmount", cupnMaxAmount);
                                        }
                                    } else if (couponPercent == 0) {
                                        if (cupnMaxAmount >= couponAmnt) {
                                            remainingTotalAmnt = cupn.getTotalAmount() - couponAmnt;
                                            coupondetails.put("appliedCouponAmount", couponAmnt);
                                        } else if (cupnMaxAmount < couponAmnt) {
                                            remainingTotalAmnt = cupn.getTotalAmount() - cupnMaxAmount;
                                            coupondetails.put("appliedCouponAmount", cupnMaxAmount);
                                        }
                                    }
                                    coupondetails.put("couponPercentage", cupndet.getCupnPrcnt());
                                    coupondetails.put("couponAmount", cupndet.getCupnAmount());
                                    coupondetails.put("remainingTotal", remainingTotalAmnt);
                                    coupondetails.put("key", "1");
                                } else {
                                    if (Integer.parseInt(cupndet.getCupnUsedCnt()) < couponUsedCount) {
                                        coupondetails.put("status", "success for specific user with Coupon count");
                                        /*coupondetails.put("maxCouponAmount",cupndet.getMaxCupnAmount());
									coupondetails.put("minCouponAmount",cupndet.getCupnMinAmnt());
									coupondetails.put("couponAmountPercentage",cupndet.getCupnPrcnt());*/
                                        float couponAmnt = Float.parseFloat(cupndet.getCupnAmount());
                                        float couponPercent = (cupn.getTotalAmount() / 100) * (Float.parseFloat(cupndet.getCupnPrcnt()));
                                        float cupnMaxAmount = cupndet.getMaxCupnAmount();
                                        float remainingTotalAmnt = 0;
                                        if (couponAmnt == 0) {
                                            if (cupnMaxAmount >= couponPercent) {
                                                remainingTotalAmnt = cupn.getTotalAmount() - couponPercent;
                                                coupondetails.put("appliedCouponAmount", couponPercent);
                                            } else if (cupnMaxAmount < couponPercent) {
                                                remainingTotalAmnt = cupn.getTotalAmount() - cupnMaxAmount;
                                                coupondetails.put("appliedCouponAmount", cupnMaxAmount);
                                            }
                                        } else if (couponPercent == 0) {
                                            if (cupnMaxAmount >= couponAmnt) {
                                                remainingTotalAmnt = cupn.getTotalAmount() - couponAmnt;
                                                coupondetails.put("appliedCouponAmount", couponAmnt);
                                            } else if (cupnMaxAmount < couponAmnt) {
                                                remainingTotalAmnt = cupn.getTotalAmount() - cupnMaxAmount;
                                                coupondetails.put("appliedCouponAmount", cupnMaxAmount);
                                            }
                                        }
                                        coupondetails.put("remainingTotal", remainingTotalAmnt);
                                        coupondetails.put("key", "1");
                                    } else {
                                        coupondetails.put("status", "failure for specifuser with Coupon count");
                                        coupondetails.put("key", "2");
                                    }
                                }
                            } else {
                                coupondetails.put("couponPercentage", cupndet.getCupnPrcnt());
                                coupondetails.put("couponAmount", cupndet.getCupnAmount());
                                coupondetails.put("status", "coupon is not applicable");
                                coupondetails.put("key", "3");
                            }
                        } else {
                            coupondetails.put("couponPercentage", cupndet.getCupnPrcnt());
                            coupondetails.put("couponAmount", cupndet.getCupnAmount());
                            coupondetails.put("status", "success");
                            float couponAmnt = Float.parseFloat(cupndet.getCupnAmount());
                            float couponPercent = (cupn.getTotalAmount() / 100) * (Float.parseFloat(cupndet.getCupnPrcnt()));
                            float cupnMaxAmount = cupndet.getMaxCupnAmount();
                            float remainingTotalAmnt = 0;
                            if (couponAmnt == 0) {
                                if (cupnMaxAmount >= couponPercent) {
                                    remainingTotalAmnt = cupn.getTotalAmount() - couponPercent;
                                    coupondetails.put("appliedCouponAmount", couponPercent);
                                } else if (cupnMaxAmount < couponPercent) {
                                    remainingTotalAmnt = cupn.getTotalAmount() - cupnMaxAmount;
                                    coupondetails.put("appliedCouponAmount", cupnMaxAmount);
                                }
                            } else if (couponPercent == 0) {
                                if (cupnMaxAmount >= couponAmnt) {
                                    remainingTotalAmnt = cupn.getTotalAmount() - couponAmnt;
                                    coupondetails.put("appliedCouponAmount", couponAmnt);
                                } else if (cupnMaxAmount < couponAmnt) {
                                    remainingTotalAmnt = cupn.getTotalAmount() - cupnMaxAmount;
                                    coupondetails.put("appliedCouponAmount", cupnMaxAmount);
                                }
                            }
                            coupondetails.put("remainingTotal", remainingTotalAmnt);
                            coupondetails.put("key", "1");
                        }
                    } else {
                        coupondetails.put("status", "your coupon code has expired");
                        coupondetails.put("key", "4");
                    }
                } else {
                    coupondetails.put("status", "your coupon not applicable with respect to minimum amount");
                    coupondetails.put("key", "5");
                }
                map.put("cupnId", cupndet.getId());
                map.put("cupnAmount", cupndet.getCupnAmount());
                map.put("cupnPrcnt", cupndet.getCupnPrcnt());
                map.put("cupnTypCode", cupndet.getCupnTypCode());
                map.put("cupnMaxAmount", cupndet.getMaxCupnAmount());
                System.out.println("Coupon Minumum Amount:" + cupndet.getCupnMinAmnt());
                map.put("cupnMinAmount", cupndet.getCupnMinAmnt());
                couponDet.add(map);
            }
            coupondetails.put("cupnDetails", couponDet);
        } else {
            coupondetails.put("key", "6");
            coupondetails.put("status", "Invalid Coupon");
        }
        return coupondetails;
    }

    /*	@RequestMapping(value = "/mobile/couponUsedCount", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> couponUsedCount(@RequestBody final ApplyCoupon cupn) {
		final Map<String, Object> coupondetails= new HashMap<String, Object>();
		Long couponUsedCount=cust.getCouponUsedCount(couponId, cupn);
		coupondetails.put("couponCount",couponUsedCount);
		return coupondetails;
	
	}*/
    public Date getIndianTimezoneDatewithTime() {
        DateFormat df = new  SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa");
        DateFormat outputformat = new  SimpleDateFormat("yyyy-MM-dd");
        String sbCurrentTimestamp = null;
        Calendar cSchedStartCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        long gmtTime = cSchedStartCal.getTime().getTime();
        long timezoneAlteredTime = gmtTime + TimeZone.getTimeZone("Asia/Calcutta").getRawOffset();
        Calendar cSchedStartCal1 = Calendar.getInstance(TimeZone.getTimeZone("Asia/Calcutta"));
        cSchedStartCal1.setTimeInMillis(timezoneAlteredTime);
        Date date = cSchedStartCal1.getTime();
        String input_crt_ts = df.format(date);
        Date outputDate = null;
        try {
            outputDate = df.parse(input_crt_ts);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        sbCurrentTimestamp = outputformat.format(outputDate);
        System.out.println("time as per as local........" + date);
        try {
            date = outputformat.parse(sbCurrentTimestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
