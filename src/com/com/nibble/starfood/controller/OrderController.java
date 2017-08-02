package com.nibble.starfood.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.nibble.starfood.ServiceI.OrderService;
import com.nibble.starfood.hibernatemodel.Address;
import com.nibble.starfood.hibernatemodel.CustBillHist;
import com.nibble.starfood.hibernatemodel.CustDet;
import com.nibble.starfood.hibernatemodel.CustOrdDet;
import com.nibble.starfood.hibernatemodel.CustOrdrs;
import com.nibble.starfood.hibernatemodel.Holidays;
import com.nibble.starfood.hibernatemodel.MealTime;
import com.nibble.starfood.hibernatemodel.PkgConfigRelt;
import com.nibble.starfood.hibernatemodel.PkgFoodItm;
import com.nibble.starfood.hibernatemodel.PkgSizTyp;
import com.nibble.starfood.util.CalculateEndDate;
import com.nibble.starfood.util.SendEmail;
import com.nibble.starfood.webservices.model.AllMenu;
import com.nibble.starfood.webservices.model.CustomerContact;
import com.nibble.starfood.webservices.model.CustOrderDetails;
import com.nibble.starfood.webservices.model.DataDetails;
import com.nibble.starfood.webservices.model.EndDateCal;
import com.nibble.starfood.webservices.model.GetFoodItems;
import com.nibble.starfood.webservices.model.ManageOrder;
import com.nibble.starfood.webservices.model.MenuDetails;
import com.nibble.starfood.webservices.model.Paymod;
import com.sun.mail.smtp.SMTPTransport;

@Controller
public class OrderController {

    @Autowired
    OrderService cust;

    @Autowired
    private JavaMailSender mailSender;

    /*@RequestMapping(value = "/mobile/FoodMenuList", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> prices(
			@RequestBody final MenuList menu) {
		final Map<String, Object> map = new HashMap<String, Object>();
		final Map<String, Object> foodlist = new HashMap<String, Object>();
		final List<Map<String, Object>> ordersList = new ArrayList<Map<String, Object>>();
		List<PkgSizTyp> menuList = cust.getMenuforSingleDay(menu);
		
		foodlist.put("packages available", menuList);
		for (PkgSizTyp menuLst : menuList) {
			final Map<String, Object> orderJson = new HashMap<String, Object>();
			if (menuList.getPkgFoodItm().getFoodTypeId() == singleDayMenu
					.getFoodType()
					&& menuList.getPkgFoodItm().getMealTimeCode() == singleDayMenu
							.getMealTime()) {

				orderJson.put("CRTID", menuLst.getCrtId());
				ordersList.add(orderJson);
			} else {

			}
		}
		map.put("crtids",ordersList);
		map.put("FoodList", menuList);
		return map;
	}*/
    @RequestMapping(value = "/mobile/FoodMenuListIOS", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> menuList(@RequestBody final GetFoodItems fooditems) {
        final Map<String, Object> map = new  HashMap<String, Object>();
        final List<Map<String, Object>> ordersList = new  ArrayList<Map<String, Object>>();
        System.out.println("meal plan type is subscription");
        List<PkgSizTyp> count = cust.getPackageCount();
        List<PkgSizTyp> menuList = cust.getPackagesMenuList(fooditems);
        System.out.println("MenuList size:" + menuList.size());
        for (PkgSizTyp pkg : menuList) {
            final Map<String, Object> orderJson = new  HashMap<String, Object>();
            orderJson.put("package id", pkg.getId());
            // orderJson.put("packagecount",count.size());
            orderJson.put("package name", pkg.getPkgSizTypDesc());
            //orderJson.put("package description",pkg.getPkgStockCount());
            orderJson.put("package stock count", pkg.getPkgStockCount());
            orderJson.put("package image", pkg.getPkgImg());
            if (fooditems.getPriceDurationId() == 1) {
                orderJson.put("price", pkg.getPkgPrice().getoneDayPrice());
            }
            if (fooditems.getPriceDurationId() == 2) {
                orderJson.put("price", pkg.getPkgPrice().getoneWeekPrice());
            }
            if (fooditems.getPriceDurationId() == 3) {
                orderJson.put("price", pkg.getPkgPrice().getoneMonthPrice());
            }
            if (fooditems.getPriceDurationId() == 0) {
                orderJson.put("packageprice", pkg.getPkgPrice());
            }
            orderJson.put("mealTimeDetails", pkg.getMealtime());
            //orderJson.put("mealCutOffTime",pkg.getMealtime().getCutoffTime());
            orderJson.put("tax", pkg.getTax());
            orderJson.put("food type", pkg.getFoodtyp());
            orderJson.put("packageDuration", pkg.getPkgDuration());
            orderJson.put("weekDuration", pkg.getWeekTyp());
            orderJson.put("menuType", pkg.getMenuTyp());
            orderJson.put("mealType", pkg.getMealType());
            //List<PkgConfigRelt> foodItems=cust.getItemsBasedOnPackage(pkg.getId(),fooditems.getDates());
            List<PkgConfigRelt> foodItems = cust.getItemsBasedOnPackage(pkg.getId(), fooditems.getDates());
            System.out.println("Config Size:" + foodItems.size());
            final List<Map<String, Object>> itemList = new  ArrayList<Map<String, Object>>();
            List<String> packageItems = new  ArrayList<String>();
            final Map<String, Object> itemmap = new  HashMap<String, Object>();
            String desc = "";
            for (PkgConfigRelt pkgsitems : foodItems) {
                if (desc != "") {
                    desc = desc + ",";
                }
                desc = desc + pkgsitems.getItems().getFoodItmNameEn();
            }
            orderJson.put("foodItemDesc", desc);
            if (desc != "") {
                ordersList.add(orderJson);
            }
        }
        if (ordersList.size() != 0) {
            map.put("PkgItemList", ordersList);
            map.put("status", "success");
            map.put("key", "1");
            map.put("delivery charge", menuList.get(0).getDelvChrg().getDelvChrg());
            map.put("online charge", menuList.get(0).getOnlinePayChrg());
        } else {
            map.put("status", "menu not found");
            map.put("key", "3");
        }
        return map;
    }

    @RequestMapping(value = "/mobile/datesBasedonCuttOffTime", method = RequestMethod.GET)
    @ResponseBody
    public Object datesBasedonCuttOffTime() {
        //		final Map<String, Object> map = new HashMap<String, Object>();
        final Map<String, Object> details = new  HashMap<String, Object>();
        List<Map<String, Object>> dateDetails = new  ArrayList<Map<String, Object>>();
        int mealPlanId = 1;
        int dinnerTimeId = 2;
        for (int i = 1; i <= 2; i++) {
            final Map<String, Object> map = new  HashMap<String, Object>();
            int mealTimeId = i;
            SimpleDateFormat simpleDateFormat = new  SimpleDateFormat("yyyy-MM-dd");
            String presentDate = simpleDateFormat.format(getIndianTimezoneDatewithTime());
            System.out.println("presentDate>>> " + presentDate);
            Holidays holidays = cust.getHolidayDet(presentDate);
            String todyDate = presentDate;
            Calendar calendr = Calendar.getInstance();
            try {
                calendr.setTime(simpleDateFormat.parse(simpleDateFormat.format(simpleDateFormat.parse(todyDate))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            DateFormat df = new  SimpleDateFormat("yyyy-MM-dd");
            DateFormat currentDay = new  SimpleDateFormat("EEEE");
            int day = calendr.get(Calendar.DAY_OF_WEEK);
            if (holidays != null) {
                calendr.add(Calendar.DATE, 1);
                System.out.println(df.format(calendr.getTime()));
                map.put("date", df.format(calendr.getTime()));
                map.put("day", currentDay.format(calendr.getTime()));
                map.put("holidayDesc", holidays.getHolidayDesc());
                map.put("key", "5");
                map.put("status", "holiday");
            } else {
                if (day == Calendar.SUNDAY) {
                    System.out.println("days are matching");
                    calendr.add(Calendar.DATE, 1);
                    System.out.println(df.format(calendr.getTime()));
                    map.put("date", df.format(calendr.getTime()));
                    map.put("day", currentDay.format(calendr.getTime()));
                    map.put("key", "4");
                    map.put("status", "sunday");
                } else if (mealPlanId == 1 || mealPlanId == 3) {
                    Date indianCurrentDate = getIndianTimezoneDatewithTime();
                    SimpleDateFormat formatter = new  SimpleDateFormat("yyyy-MM-dd");
                    String inputDate = presentDate;
                    Date inputDt = null;
                    try {
                        inputDt = formatter.parse(inputDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    boolean isBefore = true;
                    MealTime mealTimeDet = cust.getCutOffTimeBasedonMealTime(mealTimeId);
                    if (inputDt.equals(indianCurrentDate)) {
                        DateFormat dateintimeformat = new  SimpleDateFormat("hh:mm a");
                        Date cutofftimeindate = new  Date();
                        Date currenttime = getIndianTimeZoneWithDate();
                        try {
                            cutofftimeindate = dateintimeformat.parse(mealTimeDet.getCutoffTime());
                            currenttime = dateintimeformat.parse(getIndianTimeWithStringForMealTime());
                            System.out.println(currenttime + "for forLunch");
                            System.out.println(cutofftimeindate + "forLunch");
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (cutofftimeindate.getTime() - currenttime.getTime() > 0) {
                            isBefore = true;
                        } else {
                            isBefore = false;
                        }
                    }
                    if (isBefore == true) {
                        // menu list show here
                        map.put("date", presentDate);
                        if (i == 1) {
                            map.put("mealTime", "lunch");
                            map.put("status", "lunch available");
                        } else if (i == 2) {
                            map.put("mealTime", "dinner");
                            map.put("status", "dinner available");
                        }
                        map.put("key", "1");
                    } else {
                        Date currentDate = getIndianTimezoneDatewithTime();
                        Calendar calndr = Calendar.getInstance();
                        SimpleDateFormat simpleDateFormt = new  SimpleDateFormat("yyyy-MM-dd");
                        try {
                            calndr.setTime(simpleDateFormt.parse(simpleDateFormt.format(currentDate)));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        DateFormat dtformat = new  SimpleDateFormat("yyyy-MM-dd");
                        DateFormat presentDay = new  SimpleDateFormat("EEEE");
                        int dayName = calndr.get(Calendar.DAY_OF_WEEK);
                        if (dayName == Calendar.SATURDAY) {
                            System.out.println("days are matching");
                            calndr.add(Calendar.DATE, 2);
                            System.out.println(dtformat.format(calndr.getTime()));
                            map.put("date", dtformat.format(calndr.getTime()));
                            map.put("day", presentDay.format(calndr.getTime()));
                        } else {
                            System.out.println("days are not matching");
                            System.out.println("setDay " + Calendar.SUNDAY + ", currentDay " + dayName);
                            calndr.add(Calendar.DATE, 1);
                            System.out.println(dtformat.format(calndr.getTime()));
                            map.put("date", dtformat.format(calndr.getTime()));
                            map.put("day", presentDay.format(calndr.getTime()));
                        }
                        map.put("key", "2");
                        map.put("cutoffTime", mealTimeDet.getCutoffTime());
                        if (mealTimeId == 1) {
                            map.put("mealTime", "lunch");
                            map.put("status", "No more lunch order");
                        } else if (mealTimeId == 2) {
                            map.put("mealTime", "dinner");
                            map.put("status", "No more dinner order");
                        }
                    }
                }
            }
            dateDetails.add(map);
        }
        details.put("dateBasedCutOffTimeDet", dateDetails);
        return details;
    }

    @RequestMapping(value = "/mobile/FoodMenuListForPortal", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> price1(@RequestBody final GetFoodItems fooditems) {
        final Map<String, Object> map = new  HashMap<String, Object>();
        final Map<String, Object> foodlist = new  HashMap<String, Object>();
        final List<Map<String, Object>> ordersList = new  ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> ordersList1 = new  ArrayList<Map<String, Object>>();
        //List<PkgConfigRelt> foodItems;
        Holidays holidays = cust.getHolidayDet(fooditems.getDates());
        String todyDate = fooditems.getDates();
        Calendar calendr = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new  SimpleDateFormat("yyyy-MM-dd");
        //SimpleDateFormat simpleDateFormat=new SimpleDateFormat("m-d-yyyy");
        try {
            calendr.setTime(simpleDateFormat.parse(simpleDateFormat.format(simpleDateFormat.parse(todyDate))));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat df = new  SimpleDateFormat("yyyy-MM-dd");
        DateFormat currentDay = new  SimpleDateFormat("EEEE");
        int day = calendr.get(Calendar.DAY_OF_WEEK);
        if (fooditems.getMealPlanId() == 2) {
            System.out.println("meal plan type is subscription");
            //List<PkgSizTyp> count=cust.getPackageCount();
            List<PkgSizTyp> menuList = cust.getPackagesMenuList(fooditems);
            System.out.println("Menu List Size:" + menuList.size());
            for (PkgSizTyp pkg : menuList) {
                System.out.println();
                final Map<String, Object> orderJson = new  HashMap<String, Object>();
                orderJson.put("packageid", pkg.getId());
                // orderJson.put("packagecount",count.size());
                orderJson.put("packagename", pkg.getPkgSizTypDesc());
                //orderJson.put("package description",pkg.getPkgStockCount());
                orderJson.put("packagestockcount", pkg.getPkgStockCount());
                orderJson.put("packageimage", pkg.getPkgImg());
                if (fooditems.getPriceDurationId() == 1) {
                    orderJson.put("price", pkg.getPkgPrice().getoneDayPrice());
                }
                if (fooditems.getPriceDurationId() == 2) {
                    orderJson.put("price", pkg.getPkgPrice().getoneWeekPrice());
                }
                if (fooditems.getPriceDurationId() == 3) {
                    orderJson.put("price", pkg.getPkgPrice().getoneMonthPrice());
                }
                if (fooditems.getPriceDurationId() == 4) {
                    orderJson.put("price", pkg.getPkgPrice().getOneWeekFiveDayPrice());
                }
                if (fooditems.getPriceDurationId() == 5) {
                    orderJson.put("price", pkg.getPkgPrice().gettwoMonthPrice());
                }
                if (fooditems.getPriceDurationId() == 0) {
                    orderJson.put("packageprice", pkg.getPkgPrice());
                }
                //orderJson.put("mealTimeDetails",pkg.getMealtime());
                orderJson.put("tax", pkg.getTax());
                orderJson.put("foodtype", pkg.getFoodtyp());
                orderJson.put("packageDuration", pkg.getPkgDuration());
                orderJson.put("weekDuration", pkg.getWeekTyp());
                orderJson.put("menuType", pkg.getMenuTyp());
                orderJson.put("mealType", pkg.getMealType());
                //List<PkgConfigRelt> foodItems=cust.getItemsBasedOnPackage(pkg.getId(),fooditems.getDates());
                System.out.println("Date from Input:" + fooditems.getDates());
                List<PkgConfigRelt> foodItems = cust.getItemsBasedOnPackageMealTime(pkg.getId(), fooditems.getDates(), fooditems.getMealTime());
                System.out.println("Package Config Relation:" + foodItems.size());
                final List<Map<String, Object>> itemList = new  ArrayList<Map<String, Object>>();
                List<String> packageItems = new  ArrayList<String>();
                final Map<String, Object> itemmap = new  HashMap<String, Object>();
                String desc = "";
                for (PkgConfigRelt pkgsitems : foodItems) {
                    if (desc != "") {
                        desc = desc + ",";
                    }
                    desc = desc + pkgsitems.getItems().getFoodItmNameEn();
                }
                orderJson.put("foodItemDesc", desc);
                if (desc != "") {
                    ordersList.add(orderJson);
                }
            }
            if (ordersList.size() != 0) {
                map.put("PkgItemList", ordersList);
                map.put("status", "success");
                map.put("key", "1");
                map.put("deliverycharge", menuList.get(0).getDelvChrg().getDelvChrg());
                map.put("onlinecharge", menuList.get(0).getOnlinePayChrg());
            } else {
                map.put("status", "menu not found");
                map.put("key", "3");
            }
        } else if (holidays != null) {
            calendr.add(Calendar.DATE, 1);
            System.out.println(df.format(calendr.getTime()));
            map.put("date", df.format(calendr.getTime()));
            map.put("day", currentDay.format(calendr.getTime()));
            map.put("holidayDesc", holidays.getHolidayDesc());
            map.put("key", "5");
            map.put("status", "holiday");
        } else {
            if (day == Calendar.SUNDAY) {
                System.out.println("days are matching");
                calendr.add(Calendar.DATE, 1);
                System.out.println(df.format(calendr.getTime()));
                map.put("date", df.format(calendr.getTime()));
                map.put("day", currentDay.format(calendr.getTime()));
                map.put("key", "4");
                map.put("status", "sunday");
            } else if (fooditems.getMealPlanId() == 1 || fooditems.getMealPlanId() == 3) {
                Date indianCurrentDate = getIndianTimezoneDatewithTime();
                SimpleDateFormat formatter = new  SimpleDateFormat("yyyy-MM-dd");
                String inputDate = fooditems.getDates();
                Date inputDt = null;
                try {
                    inputDt = formatter.parse(inputDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                boolean isBefore = true;
                MealTime mealTimeDet = cust.getCutOffTimeBasedonMealTime(fooditems.getMealTime());
                if (inputDt.equals(indianCurrentDate)) {
                    DateFormat dateintimeformat = new  SimpleDateFormat("hh:mm a");
                    Date cutofftimeindate = new  Date();
                    Date currenttime = getIndianTimeZoneWithDate();
                    try {
                        cutofftimeindate = dateintimeformat.parse(mealTimeDet.getCutoffTime());
                        currenttime = dateintimeformat.parse(getIndianTimeWithStringForMealTime());
                        System.out.println(currenttime + "for forLunch");
                        System.out.println(cutofftimeindate + "forLunch");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (cutofftimeindate.getTime() - currenttime.getTime() > 0) {
                        isBefore = true;
                    } else {
                        isBefore = false;
                    }
                }
                if (isBefore == true) {
                    System.out.println("current time is before cutoff time>>>");
                    List<PkgSizTyp> count = cust.getPackageCount();
                    List<PkgSizTyp> menuList = cust.getPackagesMenuList(fooditems);
                    for (PkgSizTyp pkg : menuList) {
                        final Map<String, Object> orderJson = new  HashMap<String, Object>();
                        orderJson.put("packageid", pkg.getId());
                        // orderJson.put("packagecount",count.size());
                        orderJson.put("packagename", pkg.getPkgSizTypDesc());
                        //orderJson.put("package description",pkg.getPkgStockCount());
                        orderJson.put("packagestockcount", pkg.getPkgStockCount());
                        orderJson.put("packageimage", pkg.getPkgImg());
                        if (fooditems.getPriceDurationId() == 1) {
                            orderJson.put("price", pkg.getPkgPrice().getoneDayPrice());
                        }
                        if (fooditems.getPriceDurationId() == 2) {
                            orderJson.put("price", pkg.getPkgPrice().getoneWeekPrice());
                        }
                        if (fooditems.getPriceDurationId() == 3) {
                            orderJson.put("price", pkg.getPkgPrice().getoneMonthPrice());
                        }
                        if (fooditems.getPriceDurationId() == 4) {
                            orderJson.put("price", pkg.getPkgPrice().getThreedaysPrice());
                        }
                        if (fooditems.getPriceDurationId() == 0) {
                            orderJson.put("packageprice", pkg.getPkgPrice());
                        }
                        //orderJson.put("mealTimeDetails",pkg.getMealtime());
                        orderJson.put("tax", pkg.getTax());
                        orderJson.put("foodtype", pkg.getFoodtyp());
                        orderJson.put("packageDuration", pkg.getPkgDuration());
                        orderJson.put("weekDuration", pkg.getWeekTyp());
                        orderJson.put("menuType", pkg.getMenuTyp());
                        orderJson.put("mealType", pkg.getMealType());
                        //List<PkgConfigRelt> foodItems=cust.getItemsBasedOnPackage(pkg.getId(),fooditems.getDates());
                        List<PkgConfigRelt> foodItems = cust.getItemsBasedOnPackageMealTime(pkg.getId(), fooditems.getDates(), fooditems.getMealTime());
                        final List<Map<String, Object>> itemList = new  ArrayList<Map<String, Object>>();
                        List<String> packageItems = new  ArrayList<String>();
                        final Map<String, Object> itemmap = new  HashMap<String, Object>();
                        String desc = "";
                        for (PkgConfigRelt pkgsitems : foodItems) {
                            if (desc != "") {
                                desc = desc + ",";
                            }
                            /*itemmap.put("id",pkgsitems.getItems().getId());
			itemmap.put("foodItmNameEn",pkgsitems.getItems().getFoodItmNameEn());
			itemList.add(itemmap);
			packageItems.add(pkgsitems.getItems().getFoodItmNameEn());*/
                            desc = desc + pkgsitems.getItems().getFoodItmNameEn();
                        }
                        orderJson.put("foodItemDesc", desc);
                        //orderJson.put("packgeItemDeatils", packageItems);
                        if (desc != "") {
                            ordersList.add(orderJson);
                        }
                    }
                    //map.put(foodItems);
                    if (ordersList.size() != 0) {
                        map.put("PkgItemList", ordersList);
                        //map.put("ItemDetails", ordersList1);
                        map.put("status", "success");
                        map.put("key", "1");
                        map.put("deliverycharge", menuList.get(0).getDelvChrg().getDelvChrg());
                        map.put("onlinecharge", menuList.get(0).getOnlinePayChrg());
                    } else {
                        map.put("status", "menu not found");
                        map.put("key", "3");
                    }
                } else {
                    Date currentDate = getIndianTimezoneDatewithTime();
                    Calendar calndr = Calendar.getInstance();
                    SimpleDateFormat simpleDateFormt = new  SimpleDateFormat("yyyy-MM-dd");
                    try {
                        calndr.setTime(simpleDateFormt.parse(simpleDateFormt.format(currentDate)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    DateFormat dtformat = new  SimpleDateFormat("yyyy-MM-dd");
                    DateFormat presentDay = new  SimpleDateFormat("EEEE");
                    int dayName = calndr.get(Calendar.DAY_OF_WEEK);
                    if (dayName == Calendar.SATURDAY) {
                        System.out.println("days are matching");
                        calndr.add(Calendar.DATE, 2);
                        System.out.println(dtformat.format(calndr.getTime()));
                        map.put("date", dtformat.format(calndr.getTime()));
                        map.put("day", presentDay.format(calndr.getTime()));
                    } else {
                        System.out.println("days are not matching");
                        System.out.println("setDay " + Calendar.SUNDAY + ", currentDay " + dayName);
                        calndr.add(Calendar.DATE, 1);
                        System.out.println(dtformat.format(calndr.getTime()));
                        map.put("date", dtformat.format(calndr.getTime()));
                        map.put("day", presentDay.format(calndr.getTime()));
                    }
                    map.put("key", "2");
                    map.put("cutoffTime", mealTimeDet.getCutoffTime());
                    if (fooditems.getMealTime() == 1) {
                        map.put("status", "No more lunch order");
                    } else if (fooditems.getMealTime() == 2) {
                        map.put("status", "No more dinner order");
                    }
                }
            }
        }
        //map.put("PkgFoodItemList", menuList);
        return map;
    }

    @RequestMapping(value = "/mobile/FoodMenuList", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> price(@RequestBody final GetFoodItems fooditems) {
        final Map<String, Object> map = new  HashMap<String, Object>();
        final Map<String, Object> foodlist = new  HashMap<String, Object>();
        final List<Map<String, Object>> ordersList = new  ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> ordersList1 = new  ArrayList<Map<String, Object>>();
        //List<PkgConfigRelt> foodItems;
        Holidays holidays = cust.getHolidayDet(fooditems.getDates());
        String todyDate = fooditems.getDates();
        Calendar calendr = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new  SimpleDateFormat("yyyy-MM-dd");
        try {
            calendr.setTime(simpleDateFormat.parse(simpleDateFormat.format(simpleDateFormat.parse(todyDate))));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat df = new  SimpleDateFormat("yyyy-MM-dd");
        DateFormat currentDay = new  SimpleDateFormat("EEEE");
        int day = calendr.get(Calendar.DAY_OF_WEEK);
        if (fooditems.getMealPlanId() == 2) {
            System.out.println("meal plan type is subscription");
            //List<PkgSizTyp> count=cust.getPackageCount();
            List<PkgSizTyp> menuList = cust.getPackagesMenuList(fooditems);
            for (PkgSizTyp pkg : menuList) {
                final Map<String, Object> orderJson = new  HashMap<String, Object>();
                orderJson.put("package id", pkg.getId());
                // orderJson.put("packagecount",count.size());
                orderJson.put("package name", pkg.getPkgSizTypDesc());
                //orderJson.put("package description",pkg.getPkgStockCount());
                orderJson.put("package stock count", pkg.getPkgStockCount());
                orderJson.put("package image", pkg.getPkgImg());
                if (fooditems.getPriceDurationId() == 1) {
                    orderJson.put("price", pkg.getPkgPrice().getoneDayPrice());
                }
                if (fooditems.getPriceDurationId() == 2) {
                    orderJson.put("price", pkg.getPkgPrice().getoneWeekPrice());
                }
                if (fooditems.getPriceDurationId() == 3) {
                    orderJson.put("price", pkg.getPkgPrice().getoneMonthPrice());
                }
                if (fooditems.getPriceDurationId() == 4) {
                    orderJson.put("price", pkg.getPkgPrice().getThreedaysPrice());
                }
                if (fooditems.getPriceDurationId() == 0) {
                    orderJson.put("packageprice", pkg.getPkgPrice());
                }
                //orderJson.put("mealTimeDetails",pkg.getMealtime());
                orderJson.put("tax", pkg.getTax());
                orderJson.put("food type", pkg.getFoodtyp());
                orderJson.put("packageDuration", pkg.getPkgDuration());
                orderJson.put("weekDuration", pkg.getWeekTyp());
                orderJson.put("menuType", pkg.getMenuTyp());
                orderJson.put("mealType", pkg.getMealType());
                //List<PkgConfigRelt> foodItems=cust.getItemsBasedOnPackage(pkg.getId(),fooditems.getDates());
                List<PkgConfigRelt> foodItems = cust.getItemsBasedOnPackage(pkg.getId(), fooditems.getDates());
                final List<Map<String, Object>> itemList = new  ArrayList<Map<String, Object>>();
                List<String> packageItems = new  ArrayList<String>();
                final Map<String, Object> itemmap = new  HashMap<String, Object>();
                String desc = "";
                for (PkgConfigRelt pkgsitems : foodItems) {
                    if (desc != "") {
                        desc = desc + ",";
                    }
                    desc = desc + pkgsitems.getItems().getFoodItmNameEn();
                }
                orderJson.put("foodItemDesc", desc);
                if (desc != "") {
                    ordersList.add(orderJson);
                }
            }
            if (ordersList.size() != 0) {
                map.put("PkgItemList", ordersList);
                map.put("status", "success");
                map.put("key", "1");
                map.put("delivery charge", menuList.get(0).getDelvChrg().getDelvChrg());
                map.put("online charge", menuList.get(0).getOnlinePayChrg());
            } else {
                map.put("status", "menu not found");
                map.put("key", "3");
            }
        } else if (holidays != null) {
            calendr.add(Calendar.DATE, 1);
            System.out.println(df.format(calendr.getTime()));
            map.put("date", df.format(calendr.getTime()));
            map.put("day", currentDay.format(calendr.getTime()));
            map.put("holidayDesc", holidays.getHolidayDesc());
            map.put("key", "5");
            map.put("status", "holiday");
        } else {
            if (day == Calendar.SUNDAY) {
                System.out.println("days are matching");
                calendr.add(Calendar.DATE, 1);
                System.out.println(df.format(calendr.getTime()));
                map.put("date", df.format(calendr.getTime()));
                map.put("day", currentDay.format(calendr.getTime()));
                map.put("key", "4");
                map.put("status", "sunday");
            } else if (fooditems.getMealPlanId() == 1 || fooditems.getMealPlanId() == 3) {
                Date indianCurrentDate = getIndianTimezoneDatewithTime();
                SimpleDateFormat formatter = new  SimpleDateFormat("yyyy-MM-dd");
                String inputDate = fooditems.getDates();
                Date inputDt = null;
                try {
                    inputDt = formatter.parse(inputDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                boolean isBefore = true;
                MealTime mealTimeDet = cust.getCutOffTimeBasedonMealTime(fooditems.getMealTime());
                if (inputDt.equals(indianCurrentDate)) {
                    DateFormat dateintimeformat = new  SimpleDateFormat("hh:mm a");
                    Date cutofftimeindate = new  Date();
                    Date currenttime = getIndianTimeZoneWithDate();
                    try {
                        cutofftimeindate = dateintimeformat.parse(mealTimeDet.getCutoffTime());
                        currenttime = dateintimeformat.parse(getIndianTimeWithStringForMealTime());
                        System.out.println(currenttime + "for forLunch");
                        System.out.println(cutofftimeindate + "forLunch");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (cutofftimeindate.getTime() - currenttime.getTime() > 0) {
                        isBefore = true;
                    } else {
                        isBefore = false;
                    }
                }
                if (isBefore == true) {
                    System.out.println("current time is before cutoff time>>>");
                    List<PkgSizTyp> count = cust.getPackageCount();
                    List<PkgSizTyp> menuList = cust.getPackagesMenuList(fooditems);
                    for (PkgSizTyp pkg : menuList) {
                        final Map<String, Object> orderJson = new  HashMap<String, Object>();
                        orderJson.put("package id", pkg.getId());
                        // orderJson.put("packagecount",count.size());
                        orderJson.put("package name", pkg.getPkgSizTypDesc());
                        //orderJson.put("package description",pkg.getPkgStockCount());
                        orderJson.put("package stock count", pkg.getPkgStockCount());
                        orderJson.put("package image", pkg.getPkgImg());
                        if (fooditems.getPriceDurationId() == 1) {
                            orderJson.put("price", pkg.getPkgPrice().getoneDayPrice());
                        }
                        if (fooditems.getPriceDurationId() == 2) {
                            orderJson.put("price", pkg.getPkgPrice().getoneWeekPrice());
                        }
                        if (fooditems.getPriceDurationId() == 3) {
                            orderJson.put("price", pkg.getPkgPrice().getoneMonthPrice());
                        }
                        if (fooditems.getPriceDurationId() == 4) {
                            orderJson.put("price", pkg.getPkgPrice().getThreedaysPrice());
                        }
                        if (fooditems.getPriceDurationId() == 0) {
                            orderJson.put("packageprice", pkg.getPkgPrice());
                        }
                        //orderJson.put("mealTimeDetails",pkg.getMealtime());
                        orderJson.put("tax", pkg.getTax());
                        orderJson.put("food type", pkg.getFoodtyp());
                        orderJson.put("packageDuration", pkg.getPkgDuration());
                        orderJson.put("weekDuration", pkg.getWeekTyp());
                        orderJson.put("menuType", pkg.getMenuTyp());
                        orderJson.put("mealType", pkg.getMealType());
                        //List<PkgConfigRelt> foodItems=cust.getItemsBasedOnPackage(pkg.getId(),fooditems.getDates());
                        List<PkgConfigRelt> foodItems = cust.getItemsBasedOnPackage(pkg.getId(), fooditems.getDates());
                        final List<Map<String, Object>> itemList = new  ArrayList<Map<String, Object>>();
                        List<String> packageItems = new  ArrayList<String>();
                        final Map<String, Object> itemmap = new  HashMap<String, Object>();
                        String desc = "";
                        for (PkgConfigRelt pkgsitems : foodItems) {
                            if (desc != "") {
                                desc = desc + ",";
                            }
                            /*itemmap.put("id",pkgsitems.getItems().getId());
			itemmap.put("foodItmNameEn",pkgsitems.getItems().getFoodItmNameEn());
			itemList.add(itemmap);
			packageItems.add(pkgsitems.getItems().getFoodItmNameEn());*/
                            desc = desc + pkgsitems.getItems().getFoodItmNameEn();
                        }
                        orderJson.put("foodItemDesc", desc);
                        //orderJson.put("packgeItemDeatils", packageItems);
                        if (desc != "") {
                            ordersList.add(orderJson);
                        }
                    }
                    //map.put(foodItems);
                    if (ordersList.size() != 0) {
                        map.put("PkgItemList", ordersList);
                        //map.put("ItemDetails", ordersList1);
                        map.put("status", "success");
                        map.put("key", "1");
                        map.put("delivery charge", menuList.get(0).getDelvChrg().getDelvChrg());
                        map.put("online charge", menuList.get(0).getOnlinePayChrg());
                    } else {
                        map.put("status", "menu not found");
                        map.put("key", "3");
                    }
                } else {
                    Date currentDate = getIndianTimezoneDatewithTime();
                    Calendar calndr = Calendar.getInstance();
                    SimpleDateFormat simpleDateFormt = new  SimpleDateFormat("yyyy-MM-dd");
                    try {
                        calndr.setTime(simpleDateFormt.parse(simpleDateFormt.format(currentDate)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    DateFormat dtformat = new  SimpleDateFormat("yyyy-MM-dd");
                    DateFormat presentDay = new  SimpleDateFormat("EEEE");
                    int dayName = calndr.get(Calendar.DAY_OF_WEEK);
                    if (dayName == Calendar.SATURDAY) {
                        System.out.println("days are matching");
                        calndr.add(Calendar.DATE, 2);
                        System.out.println(dtformat.format(calndr.getTime()));
                        map.put("date", dtformat.format(calndr.getTime()));
                        map.put("day", presentDay.format(calndr.getTime()));
                    } else {
                        System.out.println("days are not matching");
                        System.out.println("setDay " + Calendar.SUNDAY + ", currentDay " + dayName);
                        calndr.add(Calendar.DATE, 1);
                        System.out.println(dtformat.format(calndr.getTime()));
                        map.put("date", dtformat.format(calndr.getTime()));
                        map.put("day", presentDay.format(calndr.getTime()));
                    }
                    map.put("key", "2");
                    map.put("cutoffTime", mealTimeDet.getCutoffTime());
                    if (fooditems.getMealTime() == 1) {
                        map.put("status", "No more lunch order");
                    } else if (fooditems.getMealTime() == 2) {
                        map.put("status", "No more dinner order");
                    }
                }
            }
        }
        //map.put("PkgFoodItemList", menuList);
        return map;
    }

    /*	@RequestMapping(value = "/mobile/PackageMenuList", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> price(@RequestBody final MenuList menu) {
		final Map<String, Object> map = new HashMap<String, Object>();
		//final Map<String, Object> foodlist = new HashMap<String, Object>();
		final List<Map<String, Object>> ordersList = new ArrayList<Map<String, Object>>();
		List<PkgSizTyp> menuList = cust.getPackagesMenuList(menu);
		
		//foodlist.put("packages available", menuList);
		for (PkgSizTyp menuLst : menuList) {
			final Map<String, Object> orderJson = new HashMap<String, Object>();
			
				orderJson.put("Package Id", menuLst.getId());
				orderJson.put("Package Description", menuLst.getPkgSizTypDesc());
				orderJson.put("Menu Type", menuLst.getId());
				
				orderJson.put("Package Stock Count", menuLst.getPkgStockCount());
				orderJson.put("Package Img", menuLst.getPkgImg());
				orderJson.put("Online Charges", menuLst.getOnlinePayChrg());
				//orderJson.put("Delv Charges",menuLst.getDelvChrg().getDelvChrg());
				
				if(menuLst.getPkgDurId()==1){
				//orderJson.put("Package Price", menuLst.getPkgPrice().getoneDayPrice());
				}
				else if(menuLst.getPkgDurId()==2){
				//	orderJson.put("Package Price", menuLst.getPkgPrice().getoneWeekPrice());
					}
				else if(menuLst.getPkgDurId()==3){
				//	orderJson.put("Package Price", menuLst.getPkgPrice().getoneMonthPrice());
					}
				else if(menuLst.getPkgDurId()==4){
					//orderJson.put("Package Price", menuLst.getPkgPrice().getthreeMonthPrice());
				}
				else{
					orderJson.put("Package Price", "no value available");
				}
				
				ordersList.add(orderJson);
			
		}
		map.put("Packages List", ordersList);
		return map;
	}
	@RequestMapping(value = "/mobile/PackageList", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> pricelist(@RequestBody final MenuList menu) {
		final Map<String, Object> map = new HashMap<String, Object>();
		//final Map<String, Object> foodlist = new HashMap<String, Object>();
		final List<Map<String, Object>> ordersList = new ArrayList<Map<String, Object>>();
		List<PkgSizTyp> menuList = cust.getPkg(menu);
		
		//foodlist.put("packages available", menuList);
		for (PkgSizTyp menuLst : menuList) {
			final Map<String, Object> orderJson = new HashMap<String, Object>();
			
				orderJson.put("Package Id", menuLst.getId());
				orderJson.put("Package Description", menuLst.getPkgSizTypDesc());
				orderJson.put("Menu Type", menuLst.getId());
				
				orderJson.put("Package Stock Count", menuLst.getPkgStockCount());
				orderJson.put("Package Img", menuLst.getPkgImg());
				orderJson.put("Online Charges", menuLst.getOnlinePayChrg());
				//orderJson.put("Delv Charges",menuLst.getDelvChrg().getDelvChrg());
				
				if(menuLst.getPkgDurId()==1){
				//orderJson.put("Package Price", menuLst.getPkgPrice().getoneDayPrice());
				}
				else if(menuLst.getPkgDurId()==2){
				//	orderJson.put("Package Price", menuLst.getPkgPrice().getoneWeekPrice());
					}
				else if(menuLst.getPkgDurId()==3){
				//	orderJson.put("Package Price", menuLst.getPkgPrice().getoneMonthPrice());
					}
				else if(menuLst.getPkgDurId()==4){
					//orderJson.put("Package Price", menuLst.getPkgPrice().getthreeMonthPrice());
				}
				else{
					orderJson.put("Package Price", "no value available");
				}
				
				ordersList.add(orderJson);
			
		}
		map.put("Packages List", menuList);
		return map;
	}*/
    @RequestMapping(value = "/mobile/ListOfMealPlanIos", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> mealPlanIos() {
        final Map<String, Object> map = new  HashMap<String, Object>();
        final List<Map<String, Object>> ordersList = new  ArrayList<Map<String, Object>>();
        //changed from M to MM
        SimpleDateFormat sdf = new  SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new  Date());
        List<PkgSizTyp> pkgconfig = cust.getPackageSizeDetails();
        for (PkgSizTyp pkg : pkgconfig) {
            final Map<String, Object> orderJson = new  HashMap<String, Object>();
            orderJson.put("package id", pkg.getId());
            orderJson.put("package name", pkg.getPkgSizTypDesc());
            orderJson.put("packageprice", pkg.getPkgPrice());
            orderJson.put("food type", pkg.getFoodtyp());
            final List<Map<String, Object>> itemList = new  ArrayList<Map<String, Object>>();
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(sdf.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //minus number would decrement the days
            cal.add(Calendar.DATE, 0);
            Date dateForSearchItem = cal.getTime();
            String startDateForSearch = sdf.format(dateForSearchItem);
            //minus number would decrement the days
            cal.add(Calendar.DATE, 6);
            dateForSearchItem = cal.getTime();
            String endDateForSearch = sdf.format(dateForSearchItem);
            List<PkgConfigRelt> foodItems = cust.getItemsBasedOnPackage(pkg.getId(), startDateForSearch, endDateForSearch);
            //map.put("dates",foodItems.getMenuDate());
            for (int i = 0; i < 7; i++) {
                Calendar calendar = Calendar.getInstance();
                try {
                    calendar.setTime(sdf.parse(date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //minus number would decrement the days
                calendar.add(Calendar.DATE, i);
                Date dateForItem = calendar.getTime();
                String todayDate = sdf.format(dateForItem);
                final Map<String, Object> itemmap = new  HashMap<String, Object>();
                itemmap.put("Date", todayDate);
                List<String> packageItems = new  ArrayList<String>();
                String foodItemsDet = "";
                for (PkgConfigRelt pkgsitems : foodItems) {
                    if (pkgsitems.getMenuDate().equals(todayDate)) {
                        foodItemsDet = foodItemsDet + pkgsitems.getItems().getFoodItmNameEn() + ",";
                    }
                }
                if (foodItemsDet.endsWith(",")) {
                    foodItemsDet = foodItemsDet.substring(0, foodItemsDet.length() - 1);
                }
                itemmap.put("packgeItemDeatils", foodItemsDet);
                itemList.add(itemmap);
            }
            orderJson.put("foodItems", itemList);
            ordersList.add(orderJson);
        }
        if (ordersList.size() != 0) {
            map.put("PkgItemList", ordersList);
            map.put("status", "success");
        } else {
            map.put("status", "No list available");
        }
        return map;
    }

    @RequestMapping(value = "/mobile/ListOfMealPlan", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> mealPlan() {
        final Map<String, Object> map = new  HashMap<String, Object>();
        final List<Map<String, Object>> ordersList = new  ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> ordersList1 = new  ArrayList<Map<String, Object>>();
        final Map<String, Object> orderJson1 = new  HashMap<String, Object>();
        //Changed from M to MM
        SimpleDateFormat sdf = new  SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new  Date());
        //List<PkgSizTyp> menuList = cust.getFoodMenuList(fooditems);
        List<PkgSizTyp> pkgconfig = cust.getPackageSizeDetails();
        for (PkgSizTyp pkg : pkgconfig) {
            final Map<String, Object> orderJson = new  HashMap<String, Object>();
            orderJson.put("package id", pkg.getId());
            //orderJson.put("dates", pkg.getMenuDate());
            orderJson.put("package name", pkg.getPkgSizTypDesc());
            //orderJson.put("package image",pkg.getPkgImg());
            orderJson.put("packageprice", pkg.getPkgPrice());
            //orderJson.put("tax",pkg.getTax());
            orderJson.put("food type", pkg.getFoodtyp());
            final List<Map<String, Object>> itemList = new  ArrayList<Map<String, Object>>();
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(sdf.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //minus number would decrement the days
            cal.add(Calendar.DATE, 0);
            Date dateForSearchItem = cal.getTime();
            String startDateForSearch = sdf.format(dateForSearchItem);
            System.out.println("Start Date:" + startDateForSearch);
            //minus number would decrement the days
            cal.add(Calendar.DATE, 6);
            dateForSearchItem = cal.getTime();
            String endDateForSearch = sdf.format(dateForSearchItem);
            System.out.println("End Date :" + endDateForSearch);
            List<PkgConfigRelt> foodItems = cust.getItemsBasedOnPackage(pkg.getId(), startDateForSearch, endDateForSearch);
            //map.put("dates",foodItems.getMenuDate());
            for (int i = 0; i < 7; i++) {
                Calendar calendar = Calendar.getInstance();
                try {
                    calendar.setTime(sdf.parse(date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //minus number would decrement the days
                calendar.add(Calendar.DATE, i);
                Date dateForItem = calendar.getTime();
                String todayDate = sdf.format(dateForItem);
                final Map<String, Object> itemmap = new  HashMap<String, Object>();
                itemmap.put("Date", todayDate);
                List<String> packageItems = new  ArrayList<String>();
                String foodItemsDet = "";
                for (PkgConfigRelt pkgsitems : foodItems) {
                    if (pkgsitems.getMenuDate().equals(todayDate)) {
                        foodItemsDet = foodItemsDet + pkgsitems.getItems().getFoodItmNameEn() + ",";
                    }
                }
                if (foodItemsDet.endsWith(",")) {
                    foodItemsDet = foodItemsDet.substring(0, foodItemsDet.length() - 1);
                }
                String details = foodItemsDet;
                String[] details1 = details.split(",");
                itemmap.put("packgeItemDeatils", details1);
                /* itemmap.put("packgeItemDeatils", foodItemsDet);*/
                itemList.add(itemmap);
            }
            orderJson.put("foodItems", itemList);
            ordersList.add(orderJson);
        }
        if (ordersList.size() != 0) {
            map.put("PkgItemList", ordersList);
            //map.put("ItemList",ordersList);
            map.put("status", "success");
        } else {
            map.put("status", "No list available");
        }
        return map;
    }

    @RequestMapping(value = "/mobile/ListOfMealPlanForPortal", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> mealPlanForPortal() {
        final Map<String, Object> map = new  HashMap<String, Object>();
        final List<Map<String, Object>> ordersList = new  ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> ordersList1 = new  ArrayList<Map<String, Object>>();
        final Map<String, Object> orderJson1 = new  HashMap<String, Object>();
        //Changed from M to MM
        SimpleDateFormat sdf = new  SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new  Date());
        //List<PkgSizTyp> menuList = cust.getFoodMenuList(fooditems);
        List<PkgSizTyp> pkgconfig = cust.getPackageSizeDetails();
        for (PkgSizTyp pkg : pkgconfig) {
            final Map<String, Object> orderJson = new  HashMap<String, Object>();
            orderJson.put("packageid", pkg.getId());
            //orderJson.put("dates", pkg.getMenuDate());
            orderJson.put("packagename", pkg.getPkgSizTypDesc());
            //orderJson.put("package image",pkg.getPkgImg());
            orderJson.put("packageprice", pkg.getPkgPrice());
            //orderJson.put("tax",pkg.getTax());
            orderJson.put("foodtype", pkg.getFoodtyp());
            final List<Map<String, Object>> itemList = new  ArrayList<Map<String, Object>>();
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(sdf.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //minus number would decrement the days
            cal.add(Calendar.DATE, 0);
            Date dateForSearchItem = cal.getTime();
            String startDateForSearch = sdf.format(dateForSearchItem);
            System.out.println("Start Date:" + startDateForSearch);
            //minus number would decrement the days
            cal.add(Calendar.DATE, 6);
            dateForSearchItem = cal.getTime();
            String endDateForSearch = sdf.format(dateForSearchItem);
            System.out.println("End Date :" + endDateForSearch);
            List<PkgConfigRelt> foodItemsListForLunch = cust.getItemsBasedOnPackage(pkg.getId(), startDateForSearch, endDateForSearch);
            List<PkgConfigRelt> foodItemsListForDinner = cust.getItemsBasedOnPackageDinner(pkg.getId(), startDateForSearch, endDateForSearch);
            //map.put("dates",foodItems.getMenuDate());
            for (int i = 0; i < 7; i++) {
                Calendar calendar = Calendar.getInstance();
                try {
                    calendar.setTime(sdf.parse(date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //minus number would decrement the days
                calendar.add(Calendar.DATE, i);
                Date dateForItem = calendar.getTime();
                String todayDate = sdf.format(dateForItem);
                final Map<String, Object> itemmap = new  HashMap<String, Object>();
                itemmap.put("Date", todayDate);
                List<String> packageItems = new  ArrayList<String>();
                for (PkgConfigRelt pkgsitems : foodItemsListForLunch) {
                    if (pkgsitems.getMenuDate().equals(todayDate)) {
                        //System.out.println("Food Items :"+pkgsitems.getItems().getFoodItmNameEn());
                        packageItems.add(pkgsitems.getItems().getFoodItmNameEn());
                        itemmap.put("id", pkgsitems.getItems().getId());
                        itemmap.put("foodItmNameEn", pkgsitems.getItems().getFoodItmNameEn());
                    }
                }
                for (PkgConfigRelt pkgsitems : foodItemsListForDinner) {
                    if (pkgsitems.getMenuDate().equals(todayDate)) {
                        //System.out.println("Food Items :"+pkgsitems.getItems().getFoodItmNameEn());
                        packageItems.add(pkgsitems.getItems().getFoodItmNameEn());
                        itemmap.put("id", pkgsitems.getItems().getId());
                        itemmap.put("foodItmNameEn", pkgsitems.getItems().getFoodItmNameEn());
                    }
                }
                itemmap.put("packgeItemDeatils", packageItems);
                String foodItemsDet = "";
                for (PkgConfigRelt pkgsitems : foodItemsListForLunch) {
                    if (pkgsitems.getMenuDate().equals(todayDate)) {
                        foodItemsDet = foodItemsDet + pkgsitems.getItems().getFoodItmNameEn() + ",";
                    }
                }
                String foodItemsDet2 = "";
                for (PkgConfigRelt pkgsitems : foodItemsListForDinner) {
                    if (pkgsitems.getMenuDate().equals(todayDate)) {
                        foodItemsDet2 = foodItemsDet2 + pkgsitems.getItems().getFoodItmNameEn() + ",";
                    }
                }
                List<String> strings = new  ArrayList<String>();
                List<MenuDetails> menuDetailsList = new  ArrayList<MenuDetails>();
                if (foodItemsDet.endsWith(",")) {
                    System.out.println("Food Item :" + foodItemsDet);
                    foodItemsDet = foodItemsDet.substring(0, foodItemsDet.length() - 1);
                    String[] foodItemsDet1 = foodItemsDet.split(",");
                    for (String column : foodItemsDet1) {
                        MenuDetails menuDetails = new  MenuDetails();
                        menuDetails.setName(column);
                        menuDetailsList.add(menuDetails);
                    }
                }
                List<String> strings1 = new  ArrayList<String>();
                List<MenuDetails> menuDetailsList1 = new  ArrayList<MenuDetails>();
                if (foodItemsDet2.endsWith(",")) {
                    System.out.println("Food Item :" + foodItemsDet2);
                    foodItemsDet2 = foodItemsDet2.substring(0, foodItemsDet2.length() - 1);
                    String[] foodItemsDet1 = foodItemsDet2.split(",");
                    for (String column : foodItemsDet1) {
                        MenuDetails menuDetails = new  MenuDetails();
                        menuDetails.setName(column);
                        menuDetailsList1.add(menuDetails);
                    }
                }
                itemmap.put("packgeItemDeatils", menuDetailsList);
                itemmap.put("packgeItemDeatilsForDinner", menuDetailsList1);
                itemList.add(itemmap);
            }
            orderJson.put("foodItems", itemList);
            ordersList.add(orderJson);
        }
        if (ordersList.size() != 0) {
            map.put("PkgItemList", ordersList);
            //map.put("ItemList",ordersList);
            map.put("status", "success");
        } else {
            map.put("status", "No list available");
        }
        return map;
    }

    @RequestMapping(value = "/mobile/ListOfMealPlanForPortalExtension", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> mealPlanForPortalExtension() {
        final Map<String, Object> map = new  HashMap<String, Object>();
        final List<Map<String, Object>> ordersList = new  ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> ordersList1 = new  ArrayList<Map<String, Object>>();
        final Map<String, Object> orderJson1 = new  HashMap<String, Object>();
        //Changed from M to MM
        SimpleDateFormat sdf = new  SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new  Date());
        List<PkgSizTyp> pkgconfig = cust.getPackageSizeDetails();
        for (PkgSizTyp pkg : pkgconfig) {
            final Map<String, Object> orderJson = new  HashMap<String, Object>();
            orderJson.put("packageid", pkg.getId());
            orderJson.put("packagename", pkg.getPkgSizTypDesc());
            orderJson.put("packageprice", pkg.getPkgPrice());
            orderJson.put("foodtype", pkg.getFoodtyp());
            final List<Map<String, Object>> itemList = new  ArrayList<Map<String, Object>>();
            Calendar c = GregorianCalendar.getInstance();
            // Set the calendar to monday of the current week
            System.out.println("Current week = " + Calendar.DAY_OF_WEEK);
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            System.out.println("Current week = " + Calendar.DAY_OF_WEEK);
            DateFormat df = new  SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String startDate = "", endDate = "";
            startDate = df.format(c.getTime());
            c.add(Calendar.DATE, 5);
            endDate = df.format(c.getTime());
            System.out.println("Start Date = " + startDate);
            System.out.println("End Date = " + endDate);
            List<PkgConfigRelt> foodItemsListForLunch = cust.getItemsBasedOnPackage(pkg.getId(), startDate, endDate);
            List<PkgConfigRelt> foodItemsListForDinner = cust.getItemsBasedOnPackageDinner(pkg.getId(), startDate, endDate);
            for (int i = 0; i < 6; i++) {
                final Map<String, Object> itemmap = new  HashMap<String, Object>();
                Calendar c1 = GregorianCalendar.getInstance();
                // Set the calendar to monday of the current week
                System.out.println("Current week = " + Calendar.DAY_OF_WEEK);
                c1.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                System.out.println("Current week = " + Calendar.DAY_OF_WEEK);
                DateFormat df1 = new  SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String startDate2 = "", startDate3 = df1.format(c1.getTime());
                //minus number would decrement the days
                c1.add(Calendar.DATE, i);
                Date dateForItem = c1.getTime();
                String todayDate = df1.format(c1.getTime());
                System.out.println("Date of next:" + dateForItem);
                System.out.println("Final Date:" + todayDate);
                try {
                    Calendar today = Calendar.getInstance();
                    DateFormat format = new  SimpleDateFormat("yyyy/MM/dd");
                    Date date1 = format.parse(startDate3);
                    today.setTime(date1);
                    today.add(Calendar.DATE, 1);
                    todayDate = format.format(today.getTime());
                } catch (Exception e) {
                }
                if (i == 0) {
                    itemmap.put("Day", "Monday");
                } else if (i == 1) {
                    itemmap.put("Day", "Tuesday");
                } else if (i == 2) {
                    itemmap.put("Day", "Wednesday");
                } else if (i == 3) {
                    itemmap.put("Day", "Thursday");
                } else if (i == 4) {
                    itemmap.put("Day", "Friday");
                } else if (i == 5) {
                    itemmap.put("Day", "Saturday");
                }
                itemmap.put("Date", todayDate);
                List<String> packageItems = new  ArrayList<String>();
                for (PkgConfigRelt pkgsitems : foodItemsListForLunch) {
                    if (pkgsitems.getMenuDate().equals(todayDate)) {
                        packageItems.add(pkgsitems.getItems().getFoodItmNameEn());
                        itemmap.put("id", pkgsitems.getItems().getId());
                        itemmap.put("foodItmNameEn", pkgsitems.getItems().getFoodItmNameEn());
                    }
                }
                for (PkgConfigRelt pkgsitems : foodItemsListForDinner) {
                    if (pkgsitems.getMenuDate().equals(todayDate)) {
                        packageItems.add(pkgsitems.getItems().getFoodItmNameEn());
                        itemmap.put("id", pkgsitems.getItems().getId());
                        itemmap.put("foodItmNameEn", pkgsitems.getItems().getFoodItmNameEn());
                    }
                }
                itemmap.put("packgeItemDeatils", packageItems);
                String foodItemsDet = "";
                for (PkgConfigRelt pkgsitems : foodItemsListForLunch) {
                    if (pkgsitems.getMenuDate().equals(todayDate)) {
                        foodItemsDet = foodItemsDet + pkgsitems.getItems().getFoodItmNameEn() + ",";
                    }
                }
                String foodItemsDet2 = "";
                for (PkgConfigRelt pkgsitems : foodItemsListForDinner) {
                    if (pkgsitems.getMenuDate().equals(todayDate)) {
                        foodItemsDet2 = foodItemsDet2 + pkgsitems.getItems().getFoodItmNameEn() + ",";
                    }
                }
                List<String> strings = new  ArrayList<String>();
                List<MenuDetails> menuDetailsList = new  ArrayList<MenuDetails>();
                if (foodItemsDet.endsWith(",")) {
                    System.out.println("Food Item :" + foodItemsDet);
                    foodItemsDet = foodItemsDet.substring(0, foodItemsDet.length() - 1);
                    String[] foodItemsDet1 = foodItemsDet.split(",");
                    for (String column : foodItemsDet1) {
                        MenuDetails menuDetails = new  MenuDetails();
                        menuDetails.setName(column);
                        menuDetailsList.add(menuDetails);
                    }
                }
                List<String> strings1 = new  ArrayList<String>();
                List<MenuDetails> menuDetailsList1 = new  ArrayList<MenuDetails>();
                if (foodItemsDet2.endsWith(",")) {
                    System.out.println("Food Item :" + foodItemsDet2);
                    foodItemsDet2 = foodItemsDet2.substring(0, foodItemsDet2.length() - 1);
                    String[] foodItemsDet1 = foodItemsDet2.split(",");
                    for (String column : foodItemsDet1) {
                        MenuDetails menuDetails = new  MenuDetails();
                        menuDetails.setName(column);
                        menuDetailsList1.add(menuDetails);
                    }
                }
                itemmap.put("packgeItemDeatils", menuDetailsList);
                itemmap.put("packgeItemDeatilsForDinner", menuDetailsList1);
                itemList.add(itemmap);
            }
            orderJson.put("foodItems", itemList);
            ordersList.add(orderJson);
        }
        if (ordersList.size() != 0) {
            map.put("PkgItemList", ordersList);
            map.put("status", "success");
        } else {
            map.put("status", "No list available");
        }
        return map;
    }

    /*@RequestMapping(value = "/mobile/ListOfMealPlanForPortal", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> mealPlanForPortal() {
		final Map<String, Object> map = new HashMap<String, Object>();
		final List<Map<String, Object>> ordersList = new ArrayList<Map<String, Object>>();
		final List<Map<String, Object>> ordersList1 = new ArrayList<Map<String, Object>>();
		final Map<String, Object> orderJson1 = new HashMap<String, Object>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //Changed from M to MM
		  String date = sdf.format(new Date()); 
		//List<PkgSizTyp> menuList = cust.listOfMealPlanList();
		//List<PkgSizTyp> menuList = cust.getFoodMenuList(fooditems);
		List<PkgSizTyp> pkgconfig = cust.getPackageSizeDetails();
	
		for(PkgSizTyp pkg:pkgconfig)
		{
		final Map<String, Object> orderJson = new HashMap<String, Object>();
	    orderJson.put("packageid",pkg.getId());
	    //orderJson.put("packagecount",count.size());
		//orderJson.put("dates", pkg.getMenuDate());
		orderJson.put("packagename",pkg.getPkgSizTypDesc());
		//orderJson.put("package stock count",pkg.getPkgStockCount());
		//orderJson.put("package image",pkg.getPkgImg());
		orderJson.put("packageprice",pkg.getPkgPrice());
		//orderJson.put("tax",pkg.getTax());
		orderJson.put("foodtype",pkg.getFoodtyp());
		//orderJson.put("package duration",pkg.getPkgDuration());
		//orderJson.put("week duration",pkg.getWeekTyp());
		//orderJson.put("delivery charge",pkg.getDelvChrg());
		//orderJson.put("online charge",pkg.getOnlinePayChrg());
		
		//List<PkgConfigRelt>  foodItems=cust.getListOf(pkg.getId());
		
		final List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
		
		 
	     Calendar cal = Calendar.getInstance();
	           try {
				cal.setTime(sdf.parse(date));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	           cal.add(Calendar.DATE, 0); //minus number would decrement the days
	          Date dateForSearchItem= cal.getTime();
	     String startDateForSearch=sdf.format(dateForSearchItem);
	     System.out.println("Start Date:"+startDateForSearch);
	     cal.add(Calendar.DATE, 6); //minus number would decrement the days
          dateForSearchItem= cal.getTime();
	     String endDateForSearch=sdf.format(dateForSearchItem);
	     System.out.println("End Date :"+endDateForSearch);
	     List<PkgConfigRelt>  foodItems=cust.getItemsBasedOnPackage(pkg.getId(),startDateForSearch,endDateForSearch);
	          //map.put("dates",foodItems.getMenuDate());
	    for(int i=0;i<7;i++){   
		  Calendar calendar = Calendar.getInstance();
	        try {
	        	calendar.setTime(sdf.parse(date));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        calendar.add(Calendar.DATE, i); //minus number would decrement the days
	       Date dateForItem= calendar.getTime();
	        String todayDate=sdf.format(dateForItem);
	        final Map<String, Object> itemmap = new HashMap<String, Object>();
	        itemmap.put("Date",todayDate);
	        List<String> packageItems=new ArrayList<String>();
		for(PkgConfigRelt pkgsitems:foodItems)
		{  
			
       	
       if(pkgsitems.getMenuDate().equals(todayDate)){
    	   packageItems.add(pkgsitems.getItems().getFoodItmNameEn());
			itemmap.put("id",pkgsitems.getItems().getId());
			itemmap.put("foodItmNameEn",pkgsitems.getItems().getFoodItmNameEn());
			//itemdate.put("Date",pkgsitems.getMenuDate());
			
		}
		}
		itemmap.put("packgeItemDeatils", packageItems);
	        
	        String foodItemsDet="";
		for(PkgConfigRelt pkgsitems:foodItems)
		{  
			
       	
       if(pkgsitems.getMenuDate().equals(todayDate)){
    	 //  packageItems.add(pkgsitems.getItems().getFoodItmNameEn());
    	   
    	   foodItemsDet=foodItemsDet+pkgsitems.getItems().getFoodItmNameEn()+",";
    	   
			}
		}
		if (foodItemsDet.endsWith(",")) {
			foodItemsDet = foodItemsDet.substring(0, foodItemsDet.length()-1);
		}	
	    itemmap.put("packgeItemDeatils", foodItemsDet);
		itemList.add(itemmap);
		}
		
		 orderJson.put("foodItems",itemList);
		
		ordersList.add(orderJson);
		}
		
		
		
		if(ordersList.size()!=0){
			map.put("PkgItemList", ordersList);
			
			//map.put("ItemList",ordersList);
			map.put("status","success");
			}else{
				map.put("status","No list available");
			}
		return map;
	}*/
    /*@RequestMapping(value = "mobile/orderSave", method = RequestMethod.POST)
	public @ResponseBody Object guestorderSave(@RequestBody final OrderWrapper feed) {
		
		final List<CustOrderDetails> guestCustorderlist = feed.getOrders();
		//final List<DatesList> dateList=feed.getDates();
		final List<CustOrdDet> guestorderDet = new ArrayList<CustOrdDet>();
		final Map<String,Object> map=new HashMap<String,Object>();
		//System.out.println("date"+dateList);
		for (final CustOrderDetails guestcustOrder : guestCustorderlist) {
			CustOrdDet guestOrderSave = new CustOrdDet();
			guestOrderSave = cust.saveOrderBill(guestcustOrder);
			if (guestOrderSave != null) {
				guestorderDet.add(guestOrderSave);
			}
			
		}
       
		
		map.put("list",guestorderDet);

		// order.put("orders", orderDet);
		// cust.saveBill(feed);
		return map;
	}*/
    @RequestMapping(value = "mobile/ordersave", method = RequestMethod.POST)
    @ResponseBody
    public Object orderSave(@RequestBody final CustOrderDetails feed) {
        //final CustOrderDetails custorderlist = feed.getOrders();
        final CustOrderDetails custorderlist = feed;
        List<CustOrdDet> orderDetails = new  ArrayList<CustOrdDet>();
        Map<String, Object> map = new  HashMap<String, Object>();
        final List<Map<String, Object>> ordersList = new  ArrayList<Map<String, Object>>();
        orderDetails = cust.saveBill(custorderlist);
        if (orderDetails.size() != 0) {
            for (CustOrdDet ordDet : orderDetails) {
                final Map<String, Object> orderJson = new  HashMap<String, Object>();
                //orderJson.put("menuType", ordDet.getMenuTyp().getMenuTypDesc());
                orderJson.put("pkgDurId", ordDet.getPkgDur().getId());
                orderJson.put("pkgDurdesc", ordDet.getPkgDur().getPkgDurDesc());
                orderJson.put("startDate", ordDet.getOrdStDt());
                orderJson.put("endDate", ordDet.getOrdEndDt());
                /*if(ordDet.getPayModId()==4){
			final String emailAddress = ordDet.getCustDet().getEmailId(); 
			String emailBody = "Hi " + ordDet.getCustDet().getCustName() + ",\n\n"
					+ "We are glad to have a Starfood Customer like you! \n\n"
					+ "Your order #" + ordDet.getId() + " for "
					+ ordDet.getPkgSizTypQty() + " quantity, "
					//+ order.getMealTyp().getMealTypDesc() + ", "
					+ ordDet.getFoodTyp().getFoodTypeDesc() +","
					+ ordDet.getMealTime().getMealTimeDesc()+","
					+ ordDet.getPkgDur().getPkgDurDesc()+
					" Meal has been placed.\n\n"
					+ "Your delicious Starfood Meal Plan begins from "
					+ ordDet.getOrdStDt()
					+ ".\n\n Hope you love the food!\n Happy Eating! :)\n\n"
					+ "Team StarFood!";
			sendEmail(emailAddress,emailBody);
			}*/
                ordersList.add(orderJson);
            }
            map.put("SavedOrderDetails", ordersList);
            map.put("TransactionId", cust.getBillDetailsById(orderDetails.get(0).getCustBillId()).getTransactionId());
            //map.put("customerId",orderDetails.get(0).getCustDet().getId() );
            map.put("custBillId", orderDetails.get(0).getCustBillId());
            map.put("paymodId", orderDetails.get(0).getPayModId());
            map.put("status", "success");
            map.put("key", "1");
        } else {
            map.put("key", "2");
            map.put("status", "fail");
        }
        return map;
    }

    @RequestMapping(value = "mobile/time", method = RequestMethod.GET)
    @ResponseBody
    public Object currentIstTime() {
        final Map<String, Object> map = new  HashMap<String, Object>();
        Date todayDate = getTime();
        SimpleDateFormat simpleDateFormat = new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String st = simpleDateFormat.format(todayDate);
        map.put("currentTime", st);
        System.out.println("stirng Date " + st);
        return map;
    }

    @RequestMapping(value = "mobile/manageOdrerDetails", method = RequestMethod.POST)
    @ResponseBody
    public Object manageOrder(@RequestBody final ManageOrder order) {
        List<CustBillHist> custBill = cust.getBillIdByCustId(order);
        final Map<String, Object> map = new  HashMap<String, Object>();
        final List<Map<String, Object>> billWiseList = new  ArrayList<Map<String, Object>>();
        //final List<List<HashMap<String, Object>>> billWiseList1 = new ArrayList<List<HashMap<String, Object>>>();
        for (CustBillHist custBillDet : custBill) {
            final Map<String, Object> billDet = new  HashMap<String, Object>();
            final List<Map<String, Object>> lunchList = new  ArrayList<Map<String, Object>>();
            final List<Map<String, Object>> dinnerList = new  ArrayList<Map<String, Object>>();
            List<CustOrdrs> custOrd = cust.getOrderDetailsByMealTime(custBillDet.getId());
            for (CustOrdrs ord : custOrd) {
                List<CustOrdDet> custOrders = cust.getOrderDetailsByOrderNo(ord.getOrdNo());
                for (CustOrdDet orderDet : custOrders) {
                    final Map<String, Object> lunchOrderDetails = new  HashMap<String, Object>();
                    final Map<String, Object> dinnerOrderDetails = new  HashMap<String, Object>();
                    //System.out.println("pkagId   :  "+i+"     "+orderDet.getId());
                    System.out.println("000values>>" + orderDet.getPkgSize().getId());
                    System.out.println("Order Status Id:" + orderDet.getOrdrStId());
                    System.out.println("Paymod Id:" + orderDet.getPayModId());
                    if (orderDet.getMealTime().getId() == 1) {
                        lunchOrderDetails.put("pkgId", orderDet.getPkgSize().getId());
                        lunchOrderDetails.put("orderedDate", orderDet.getOrdDt());
                        lunchOrderDetails.put("orderStartDate", orderDet.getOrdStDt());
                        lunchOrderDetails.put("orderEndDate", orderDet.getOrdEndDt());
                        lunchOrderDetails.put("pkgName", orderDet.getPkgSize().getPkgSizTypDesc());
                        lunchOrderDetails.put("pkgDurationDesc", orderDet.getPkgDur().getPkgDurDesc());
                        lunchOrderDetails.put("weekTypDesc", orderDet.getWeekTyp().getWeekTypDesc());
                        lunchOrderDetails.put("pkgQuantity", orderDet.getPkgSizTypQty());
                        lunchOrderDetails.put("pkgAmount", orderDet.getOrdBillAmnt());
                        System.out.println("Pkg Amount:" + orderDet.getOrdBillAmnt());
                        if (orderDet.getPayModId() == 4) {
                            lunchOrderDetails.put("paymentstatus", "Cash On Delivery-UnPaid");
                        } else if (orderDet.getPayModId() == 3) {
                            lunchOrderDetails.put("paymentstatus", "Cash On Delivery-Paid");
                        } else if (orderDet.getPayModId() == 5) {
                            lunchOrderDetails.put("paymentstatus", "Online-UnPaid");
                        } else if (orderDet.getPayModId() == 6) {
                            lunchOrderDetails.put("paymentstatus", "Online-Paid");
                        }
                        lunchList.add(lunchOrderDetails);
                    }
                    if (orderDet.getMealTime().getId() == 2) {
                        dinnerOrderDetails.put("pkgId", orderDet.getPkgSize().getId());
                        dinnerOrderDetails.put("orderedDate", orderDet.getOrdDt());
                        dinnerOrderDetails.put("orderStartDate", orderDet.getOrdStDt());
                        dinnerOrderDetails.put("orderEndDate", orderDet.getOrdEndDt());
                        dinnerOrderDetails.put("pkgName", orderDet.getPkgSize().getPkgSizTypDesc());
                        dinnerOrderDetails.put("pkgDurationDesc", orderDet.getPkgDur().getPkgDurDesc());
                        dinnerOrderDetails.put("weekTypDesc", orderDet.getWeekTyp().getWeekTypDesc());
                        dinnerOrderDetails.put("pkgQuantity", orderDet.getPkgSizTypQty());
                        dinnerOrderDetails.put("pkgAmount", orderDet.getOrdBillAmnt());
                        if (orderDet.getPayModId() == 4) {
                            dinnerOrderDetails.put("paymentstatus", "Cash On Delivery-UnPaid");
                        } else if (orderDet.getPayModId() == 3) {
                            dinnerOrderDetails.put("paymentstatus", "Cash On Delivery-Paid");
                        } else if (orderDet.getPayModId() == 5) {
                            dinnerOrderDetails.put("paymentstatus", "Online-Paid");
                        } else if (orderDet.getPayModId() == 6) {
                            dinnerOrderDetails.put("paymentstatus", "Online-UnPaid");
                        }
                        dinnerList.add(dinnerOrderDetails);
                    }
                }
            }
            billDet.put("billId", custBillDet.getId());
            billDet.put("orderStartDate", custBillDet.getOrdStDt());
            billDet.put("orderEndDate", custBillDet.getOrdEndDt());
            billDet.put("subAmount", custBillDet.getSubAmnt());
            billDet.put("grandTotal", custBillDet.getGrndTot());
            billDet.put("subGrandTotal", custBillDet.getSubGrndTot());
            billDet.put("cupnDiscAmnt", custBillDet.getCupnDiscAmnt());
            billDet.put("redeemCancelAmnt", custBillDet.getRedeemCancelAmnt());
            billDet.put("deliveryChrges", custBillDet.getDelChrg());
            billDet.put("taxAmount", custBillDet.getTaxAmnt());
            billDet.put("lunchOrdersList", lunchList);
            billDet.put("dinnerOrdersList", dinnerList);
            billWiseList.add(billDet);
        }
        if (billWiseList.size() > 0) {
            map.put("billWiseList", billWiseList);
            map.put("key", "1");
        } else {
            map.put("key", "2");
        }
        /*for(List<HashMap<String, Object>> ord:paidOrderslist){
			for(HashMap<String, Object> eachOrdDet: ord.get(0)){
				
			}
		}*/
        return map;
    }

    @RequestMapping(value = "/mobile/dataDetails", method = RequestMethod.GET)
    @ResponseBody
    public Object dataDetails() {
        /*for(Cupn cupndata:cupn)
		{
			final Map<String, Object> cupnDetailMap = new HashMap<String, Object>();
			cupnDetailMap.put("CupnId",cupndata.getId());
			cupnDetailMap.put("CupnTypeId",cupndata.getCupnTypId());
			//cupnDetailMap.put("UpdateId",cupndata.getUpdId());
			cupnDetailMap.put("CupnAmount",cupndata.getCupnAmount());
			cupnDetailMap.put("CupnPercent",cupndata.getCupnPrcnt());
			cupnDetailMap.put("CupnAmountType",cupndata.getCupnAmountTypF());
			cupnDetailMap.put("CupnCatgId",cupndata.getCupnCatgId());
			cupnDetailMap.put("CupnTypCode",cupndata.getCupnTypCode());
			cupnDetailMap.put("CupnMinAmnt",cupndata.getCupnMinAmnt());
			cupnDetailMap.put("CupnStartDate",cupndata.getCupnStrtDt());
			cupnDetailMap.put("CupnEndDate",cupndata.getCupnEndDt());
			cupnDetailMap.put("UnlimtCupnFlag",cupndata.getUnltdCupnF());
			cupnDetailMap.put("CupnMaxAmount",cupndata.getMaxCupnAmount());
			
			//cupnDetailMap.put("CustDetId",cupndata.getCustDetId());
			//cupnDetailMap.put("PkgSizTypId",cupndata.getPkgSizTypId());
			cupnDetailMap.put("CupnUsedCnt",cupndata.getCupnUsedCnt());
			cupnDetailMap.put("PkgDurId",cupndata.getPkgDurId());
			
			cupanList.add(cupnDetailMap);
		}*/
        final Map<String, Object> list = new  HashMap<String, Object>();
        Date todayDate = getTime();
        SimpleDateFormat simpleDateFormat = new  SimpleDateFormat("yyyy-MM-dd");
        String systemDate = simpleDateFormat.format(todayDate);
        SimpleDateFormat simpletime = new  SimpleDateFormat("HH:mm:ss");
        String systemTime = simpletime.format(todayDate);
        //HH for hour of the day (0 - 23)
        DateFormat f1 = new  SimpleDateFormat("HH:mm:ss");
        Date d = null;
        try {
            d = f1.parse(systemTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat f2 = new  SimpleDateFormat("hh:mm:ss a");
        String time_12Hr_format = f2.format(d);
        System.out.println("12 hour format   " + time_12Hr_format);
        list.put("MealTimeList", cust.getMealTimeList());
        list.put("Payment Gate Way", cust.getPaymentGateWay());
        list.put("MealDurationList", cust.getMealDurationList());
        list.put("WeekDurationList", cust.getWeekDurationList());
        list.put("ClientDetails", cust.getClientDetails());
        list.put("systemDate", systemDate);
        list.put("systemTime", time_12Hr_format);
        list.put("FoodTypeList", cust.getFoodTypeList());
        list.put("MealPlanList", cust.getMealPlanList());
        list.put("MenuTypeList", cust.getMenuTypeList());
        list.put("DeliveryChargesList", cust.getDeliveryChargesList());
        list.put("PaymentTypeList", cust.getPaymentTypeList());
        list.put("TaxTypeList", cust.getTaxList());
        return list;
    }

    @RequestMapping(value = "/mobile/walletdetail", produces = { MediaType.APPLICATION_JSON_VALUE }, method = RequestMethod.POST)
    @ResponseBody
    public Object walletList(@RequestBody DataDetails data) {
        final Map<String, Object> walletAmountDet = new  HashMap<String, Object>();
        final CustDet walletdetwithId = cust.getWalletDetailsById(data.getCustId());
        if (walletdetwithId != null) {
            walletAmountDet.put("walletAmount", walletdetwithId.getCupnRedmAmnt());
            walletAmountDet.put("key", "1");
        } else {
            walletAmountDet.put("key", "2");
        }
        return walletAmountDet;
    }

    /*@RequestMapping(value = "mobile/singleordersave", method = RequestMethod.POST)
	public @ResponseBody Object orderSave(@RequestBody final OrderWrapper feed) {
		final List<CustOrderDetails> custorderlist = feed.getOrders();
		final List<CustOrdDet> orderDet = new ArrayList<CustOrdDet>();
		List<CustOrdDet> orderDetails = new ArrayList<CustOrdDet>();
		Map<String,Object> map=new HashMap<String,Object>();
		//List<CustOrdDet> list=cust.listOfOrder();
		for(CustOrdDet showMenu:list)
		{
			map.put("list",showMenu.getAddressId());
		}
		orderDetails=cust.saveBill(custorderlist);
		
		map.put("id", orderDetails.get(0).getId());
		map.put("custBillId", orderDetails.get(0).getCustBillId());
		
		
		map.put("",orderDetails);
		//map.put("Packag Dur",)
		return map;
	}*/
    @RequestMapping(value = "/mobile/MealTimeList", produces = { MediaType.APPLICATION_JSON_VALUE }, method = RequestMethod.GET)
    @ResponseBody
    public Object mealTimeList() {
        final Map<String, Object> mealTimeList = new  HashMap<String, Object>();
        mealTimeList.put("MealTimeList", cust.getMealTimeList());
        return mealTimeList;
    }

    @RequestMapping(value = "/mobile/packageDurationList", produces = { MediaType.APPLICATION_JSON_VALUE }, method = RequestMethod.GET)
    @ResponseBody
    public Object packageDurationList() {
        final Map<String, Object> packageDurationList = new  HashMap<String, Object>();
        packageDurationList.put("PackageDurationList", cust.getMealDurationList());
        return packageDurationList;
    }

    @RequestMapping(value = "/mobile/packageDurations", produces = { MediaType.APPLICATION_JSON_VALUE }, method = RequestMethod.GET)
    @ResponseBody
    public Object packageDurationListOrder() {
        final Map<String, Object> packageDurationList = new  HashMap<String, Object>();
        packageDurationList.put("PackageDurationList", cust.getPackageDurationList());
        return packageDurationList;
    }

    @RequestMapping(value = "/mobile/endDate", produces = { MediaType.APPLICATION_JSON_VALUE }, method = RequestMethod.GET)
    @ResponseBody
    public Object endDate(@RequestBody EndDateCal endDateCal) {
        //format of yyyy-MM-dd
        String orderStartDateString = endDateCal.getOrderStartDate();
        int numberOfDays = 0;
        int noOfDaysInWeek = 0;
        /*if(endDateCal.getWeekDayId()==1 && endDateCal.getDurationId()==1){
			numberOfDays=6;
			noOfDaysInWeek=5;
		}*/
        if (endDateCal.getDurationId() == 1) {
            numberOfDays = 6;
            noOfDaysInWeek = 6;
        }
        if (endDateCal.getDurationId() == 2) {
            numberOfDays = 29;
            noOfDaysInWeek = 6;
        }
        String orderEndDate = CalculateEndDate.endDate(orderStartDateString, numberOfDays, noOfDaysInWeek);
        return orderEndDate;
    }

    @RequestMapping(value = "/mobile/PackageDurationList", method = RequestMethod.GET)
    @ResponseBody
    public Object mealDurationList() {
        final Map<String, Object> mealDurationList = new  HashMap<String, Object>();
        mealDurationList.put("MealDurationList", cust.getMealDurationList());
        return mealDurationList;
    }

    @RequestMapping(value = "/mobile/WeekDurationList", method = RequestMethod.GET)
    @ResponseBody
    public Object weekDurationList() {
        final Map<String, Object> weekDurationList = new  HashMap<String, Object>();
        weekDurationList.put("WeekDurationList", cust.getWeekDurationList());
        return weekDurationList;
    }

    @RequestMapping(value = "/mobile/FoodTypeList", method = RequestMethod.GET)
    @ResponseBody
    public Object foodTypeList() {
        final Map<String, Object> foodTypeList = new  HashMap<String, Object>();
        foodTypeList.put("FoodTypeList", cust.getFoodTypeList());
        return foodTypeList;
    }

    @RequestMapping(value = "/mobile/MenuTypeList", method = RequestMethod.GET)
    @ResponseBody
    public Object menuTypeList() {
        final Map<String, Object> menuTypeList = new  HashMap<String, Object>();
        menuTypeList.put("MenuTypeList", cust.getMenuTypeList());
        return menuTypeList;
    }

    @RequestMapping(value = "/mobile/DeliveryChargesList", method = RequestMethod.GET)
    @ResponseBody
    public Object deliveryChargesList() {
        final Map<String, Object> deliveryChargesList = new  HashMap<String, Object>();
        deliveryChargesList.put("DeliveryChargesList", cust.getDeliveryChargesList());
        return deliveryChargesList;
    }

    @RequestMapping(value = "/mobile/PaymentTypeList", method = RequestMethod.GET)
    @ResponseBody
    public Object paymentTypeList() {
        final Map<String, Object> paymentTypeList = new  HashMap<String, Object>();
        paymentTypeList.put("PaymentTypeList", cust.getPaymentTypeList());
        return paymentTypeList;
    }

    @RequestMapping(value = "/mobile/tax", method = RequestMethod.GET)
    @ResponseBody
    public Object taxList() {
        final Map<String, Object> taxList = new  HashMap<String, Object>();
        taxList.put("TaxTypeList", cust.getTaxList());
        return taxList;
    }

    @RequestMapping(value = "/mobile/allMenuList", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> allMenuList(@RequestBody final AllMenu menu) {
        final Map<String, Object> map = new  HashMap<String, Object>();
        //final Map<String, Object> foodlist = new HashMap<String, Object>();
        final List<Map<String, Object>> ordersList = new  ArrayList<Map<String, Object>>();
        List<PkgSizTyp> menuList = cust.getPackagesMenuList(menu);
        List<PkgFoodItm> itemList = cust.getItemList(menu);
        //foodlist.put("packages available", menuList);
        for (PkgSizTyp menuLst : menuList) {
            final Map<String, Object> orderJson = new  HashMap<String, Object>();
            orderJson.put("Package Id", menuLst.getId());
            orderJson.put("Package Description", menuLst.getPkgSizTypDesc());
            //orderJson.put("Menu Type", menuLst.getMenuTypId());
            orderJson.put("Items Count", menuLst.getPkgStockCount());
            //orderJson.put("Package Stock Count", menuLst.getPkgStockCount());
            orderJson.put("Package Img", menuLst.getPkgImg());
            ordersList.add(orderJson);
        }
        for (PkgFoodItm item : itemList) {
            final Map<String, Object> orderJson = new  HashMap<String, Object>();
            orderJson.put("Item Id", item.getId());
            orderJson.put("Package Description", item.getFoodItmNameEn());
            orderJson.put("Items Count", item.getServCnt());
            orderJson.put("Item Price", item.getFoodItmPrc());
            orderJson.put("Image", item.getImage());
            ordersList.add(orderJson);
        }
        map.put("Packages List", ordersList);
        return map;
    }

    @RequestMapping(value = "/mobile/dates", method = RequestMethod.GET)
    @ResponseBody
    public Object dates() {
        Date todayDate1 = getIndianTimezoneDatewithTime();
        List<String> menuDates = new  ArrayList<String>();
        Map<String, Object> map = new  HashMap<String, Object>();
        int j = 7;
        for (int menuDateCount = 1; menuDateCount <= j; menuDateCount++) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(todayDate1);
            int dayOfWeek = calendar.get(calendar.DAY_OF_WEEK);
            calendar.add(calendar.DATE, 1);
            Date tomorrowDate = calendar.getTime();
            //System.out.println("tomorrowDate"+tomorrowDate);
            Date tomorrowDt = null;
            SimpleDateFormat formatter = new  SimpleDateFormat("yyyy-MM-dd");
            try {
                tomorrowDt = formatter.parse(formatter.format(tomorrowDate));
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            if (menuDateCount == 1 && dayOfWeek != Calendar.SUNDAY) {
                String date = formatter.format(todayDate1);
                menuDates.add(date);
            } else if (menuDateCount == 1 && dayOfWeek == Calendar.SUNDAY) {
                j = j + 1;
            }
            int dayOfWeek1 = calendar.get(calendar.DAY_OF_WEEK);
            if (dayOfWeek1 != Calendar.SUNDAY) {
                String date = formatter.format(tomorrowDt);
                menuDates.add(date);
            }
            todayDate1 = tomorrowDt;
        }
        map.put("datesList", menuDates);
        return map;
    }

    /*@RequestMapping(value = "/mobile/paymod", method = RequestMethod.POST)
	public @ResponseBody Object payMod(@RequestBody Paymod paymod) {
		final Map<String, String> paymode = new HashMap<String, String>();

		int i = cust.updateOrderPaymode(paymod);
		if (i == 1) {

			List<CustOrdDet> custOrderDetails = cust.getCustomerOrders(paymod
					.getCustBillId());
			// SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			if (custOrderDetails.size() != 0) {
				for (CustOrdDet orderDetails : custOrderDetails) {
					String orderStartDate = orderDetails.getOrdStDt();
					Date todayDate = getIndianTimezoneDatewithTime();
					System.out.println("todayDate>>>>>>>+" + todayDate);
					Date orderStartDt = null;
					try {
						DateFormat formatType = new SimpleDateFormat(
								"yyyy-MM-dd hh:mm:ss");
						DateFormat requiredFormat = new SimpleDateFormat(
								"yyyy-MM-dd");
						orderStartDt = requiredFormat.parse(requiredFormat
								.format(formatType.parse(orderStartDate)));
						// DateFormat time = new SimpleDateFormat("hh:mm:ss a");
						System.out.println("Date: "
								+ orderStartDt);
						// System.out.println("Time: " + time.format(d));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					if (orderStartDt.equals(todayDate)) {
						PkgSizTyp updatePackageStockCount = cust.updatePackageStockCount(orderDetails
										.getPkgSize().getId(),orderDetails.getPkgSizTypQty());
						System.out.println("packageStockCount"+updatePackageStockCount);
					}
				}
			}

			paymode.put("status", "success");
			paymode.put("key", "1");
		} else {
			paymode.put("status", "failure");
			paymode.put("key", "2");
		}

		return paymode;

	}*/
    @RequestMapping(value = "/mobile/paymod", method = RequestMethod.POST)
    @ResponseBody
    public Object payMod(@RequestBody Paymod paymod) {
        // final Map<String, String> paymode = new HashMap<String, String>();
        final Map<String, Object> map = new  HashMap<String, Object>();
        int i = cust.updateOrderPaymode(paymod);
        if (i == 1) {
            List<CustOrdDet> custOrderDetails = cust.getCustomerOrders(paymod.getCustBillId());
            String emailBody = "";
            String messageBody = "";
            String emailOrderDetBody = "";
            String messageOrderDetBody = "";
            String messageOrderDetBodyForAdmin = "";
            int j = 1;
            if (custOrderDetails.size() != 0) {
                for (CustOrdDet orderDetails : custOrderDetails) {
                    Address address = cust.getAddressDet(orderDetails.getAddressId());
                    // starts sms.......
                    emailOrderDetBody = emailOrderDetBody + "Details of Package No. " + j + ": " + "\n" + "Package Name: " + orderDetails.getPkgSize().getPkgSizTypDesc() + "\n" + "Package Quantity: " + orderDetails.getPkgSizTypQty() + "\n" + "Meal Time: " + orderDetails.getMealTime().getMealTimeDesc() + "\n" + "Meal Duration: " + orderDetails.getPkgDur().getPkgDurDesc() + //+ "\n" + "Order Id: " + orderDetails.getId()
                    "\n" + "Start Date: " + orderDetails.getOrdStDt() + "\n" + "End Date: " + orderDetails.getOrdEndDt() + "\n\n";
                    messageOrderDetBody = messageOrderDetBody + "Details of Package No. " + j + ": " + "\n" + "Meal Time: " + orderDetails.getMealTime().getMealTimeDesc() + //+ "\n" + "Order Id: " + orderDetails.getId() + "\n"
                    "Start Date: " + orderDetails.getOrdStDt() + "\n" + "End Date: " + orderDetails.getOrdEndDt() + "\n\n";
                    messageOrderDetBodyForAdmin = messageOrderDetBodyForAdmin + "Details of Package No. " + j + ": " + "\n" + "Meal Time: " + orderDetails.getMealTime().getMealTimeDesc() + //+ "\n" + "Order Id: " + orderDetails.getId() + "\n"
                    "Start Date: " + orderDetails.getOrdStDt() + "\n" + "End Date: " + orderDetails.getOrdEndDt() + "\n" + "Address: " + "\n" + address.getCity().getCityDesc() + "," + address.getArea().getAreaDesc() + "," + address.getLandMrk() + "\n\n";
                    if (paymod.getPaymodeId() != 4 && paymod.getPaymodeId() != 2 && paymod.getPaymodeId() != 6) {
                        emailBody = emailBody + "Details of Package No. " + j + ":" + "\n" + "Package Name: " + orderDetails.getPkgSize().getPkgSizTypDesc() + "\n" + "Meal Time: " + orderDetails.getMealTime().getMealTimeDesc() + //	+ "\n" + "Order Id: " + orderDetails.getId()
                        "\n" + "Start Date: " + orderDetails.getOrdStDt() + "\n" + "End Date: " + orderDetails.getOrdEndDt() + "\n\n";
                        messageBody = messageBody + "Details of Package No. " + j + ": " + "\n" + "Meal Time: " + orderDetails.getMealTime().getMealTimeDesc() + //	+ "\n" + "Order Id: " + orderDetails.getId()
                        "\n" + "Start Date: " + orderDetails.getOrdStDt() + "\n" + "End Date: " + orderDetails.getOrdEndDt() + "\n\n";
                    }
                    /*
					 * emailBody =
					 * emailBody+"Details of No. "+j+" Package: "+"<br>"
					 * +"Package Name: "
					 * +orderDetails.getPkgSize().getPkgSizTypDesc
					 * ()+"<br>"+"Package Duration: "
					 * +orderDetails.getPkgDur().getPkgDurDesc
					 * ()+"<br>"+"Food Type: "
					 * +orderDetails.getFoodTyp().getFoodTypeDesc
					 * ()+"<br>"+"Package Quantity: "
					 * +orderDetails.getPkgSizTypQty()+"<br>"+"Meal Time: "
					 * +orderDetails
					 * .getMealTime().getMealTimeDesc()+"<br>"+"Start Date: "
					 * +orderDetails
					 * .getOrdStDt()+"<br>"+"End Date"+orderDetails.
					 * getOrdEndDt()+"<br><br>"; messageBody =
					 * messageBody+"Details of No. "
					 * +j+" Package: "+"\n"+"Package Name: "
					 * +orderDetails.getPkgSize
					 * ().getPkgSizTypDesc()+"\n"+"Package Duration: "
					 * +orderDetails
					 * .getPkgDur().getPkgDurDesc()+"\n"+"Food Type: "
					 * +orderDetails
					 * .getFoodTyp().getFoodTypeDesc()+"\n"+"Package Quantity: "
					 * +orderDetails.getPkgSizTypQty()+"\n"+"Meal Time: "
					 * +orderDetails
					 * .getMealTime().getMealTimeDesc()+"\n"+"Start Date: "
					 * +orderDetails
					 * .getOrdStDt()+"\n"+"End Date"+orderDetails.getOrdEndDt
					 * ()+"\n\n";
					 */
                    j++;
                }
            }
            CustBillHist billDet = cust.getCustBillDetById(paymod.getCustBillId());
            map.put("status", "success");
            map.put("key", "1");
            if (paymod.getPaymodeId() != 4 && paymod.getPaymodeId() != 2 && paymod.getPaymodeId() != 6) {
                messageBody = "Hi " + custOrderDetails.get(0).getCustDet().getCustName() + ",\n" + "We have received a payment of Rs. " + billDet.getSubGrndTot() + " for your following ordered packages" + "\n\n" + "Order Bill No. " + billDet.getId() + "\n" + messageBody + "\n" + "Team StarFood";
                // email sending for order details
                final String emailAddress = custOrderDetails.get(0).getCustDet().getEmailId();
                emailBody = "Hi " + custOrderDetails.get(0).getCustDet().getCustName() + ",\n" + "We have received a payment of Rs. " + billDet.getSubGrndTot() + " for your following ordered packages" + "\n\n" + "Order Bill No. " + billDet.getId() + "\n" + emailBody + "\n\n" + "Now it's time for you to sit back and enjoy your daily healthy, tasty meals, while our culinary experts at StarFood strive to come up with their innovative recipes to tickle your taste buds!" + "\n\n\n\n" + "Let's eat healthy and be StarFood!" + "\n\n" + "Team StarFood";
                // final String emailAddress = ordDet.getCustDet().getEmailId();
                final String subject = "Confirmation of Payment against Order Number " + billDet.getId() + " at StarFood";
                SendEmail sendEmail = new  SendEmail();
                sendEmail.mailSending(emailAddress, subject, emailBody);
            } else {
                String messageOrderDetailsBody = "Hi " + custOrderDetails.get(0).getCustDet().getCustName() + ",\n" + "Thank you for placing order" + "\n" + "We'll confirm your order shortly" + "\n" + "Order Bill No. " + billDet.getId() + "\n" + "Order Total Amount." + billDet.getSubAmnt() + "\n" + "Discount Amount." + billDet.getCupnDiscAmnt() + "\n" + "Order Bill Amount " + billDet.getSubGrndTot() + "\n" + "you have ordered following packages" + "\n\n" + messageOrderDetBody + "\n" + "Team StarFood";
                // email sending for order details
                final String emailAddress = custOrderDetails.get(0).getCustDet().getEmailId();
                String emailOrderDetailsBody = "Hi " + custOrderDetails.get(0).getCustDet().getCustName() + ",\n\n" + "Thank you for placing order" + "\n\n" + "We'll confirm your order shortly" + "\n\n" + "Order Bill No. " + custOrderDetails.get(0).getCustBillId() + "\n" + "Order Total Amount. " + billDet.getSubAmnt() + "\n" + "Discount Amount. " + billDet.getCupnDiscAmnt() + "\n" + "Order Bill Amount " + billDet.getSubGrndTot() + "\n\n" + "you have ordered following packages" + "\n\n" + emailOrderDetBody + "\n\n" + "Now it's time for you to sit back and enjoy your daily healthy, tasty meals, while our culinary experts at StarFood strive to come up with their innovative recipes to tickle your taste buds!" + "\n\n\n\n" + "Let's eat healthy and be StarFood!" + "\n\n" + "Team StarFood";
                final String Subject = "Order Placed Details";
                SendEmail sendEmail = new  SendEmail();
                sendEmail.mailSending(emailAddress, Subject, emailOrderDetailsBody);
            }
            //   final String emailAdmin = "Tandurust@hotmail.com"; 
            final String emailAdmin = "abhijitbose1@gmail.com";
            final String emailAdmin1 = "info@starfood.in";
            final String emailAdmin2 = "a.bose.mit@gmail.com";
            String adminEmailOrderDetBody = "Hi " + "Admin" + ",\n\n" + "Customer placed order" + "\n\n" + "Please look into order details as follows: " + "\n\n" + "Order Bill No. " + custOrderDetails.get(0).getCustBillId() + "\n" + "Order Bill Amount " + billDet.getSubGrndTot() + "\n" + "customer placed following packages" + "\n\n" + emailOrderDetBody + "\n\n" + "Now it's time for you to sit back and enjoy your daily healthy, tasty meals, while our culinary experts at StarFood strive to come up with their innovative recipes to tickle your taste buds!" + "\n\n\n\n" + "Let's eat healthy and be StarFood!" + "\n\n" + "Team StarFood";
            final String adminSubject = "Customer Order Placed Details";
            //sendEmail(emailAdmin,adminEmailOrderDetBody,adminSubject);
            SendEmail sendEmail = new  SendEmail();
            sendEmail.mailSending(emailAdmin, adminSubject, adminEmailOrderDetBody);
            sendEmail.mailSending(emailAdmin1, adminSubject, adminEmailOrderDetBody);
            sendEmail.mailSending(emailAdmin2, adminSubject, adminEmailOrderDetBody);
            String adminMessageOrderDetBody = "Hi " + "Admin" + ",\n" + "Customer placed order" + "\n" + "Please look into order details as follows: " + "\n" + "Order Bill No. " + billDet.getId() + "\n" + "Order Bill Amount " + billDet.getSubGrndTot() + "\n" + "Customer Name: " + custOrderDetails.get(0).getCustDet().getCustName() + "\n" + "Customer Mobile No: " + custOrderDetails.get(0).getCustDet().getCntcNo() + "\n" + "customer placed following packages" + "\n\n" + messageOrderDetBodyForAdmin + "\n" + "Team StarFood";
        } else {
            map.put("status", "failure");
            map.put("key", "2");
        }
        // list need to add::::::
        List<CustBillHist> custBill = cust.getBillDetById(paymod.getCustBillId());
        final List<Map<String, Object>> billWiseList = new  ArrayList<Map<String, Object>>();
        // ArrayList<List<HashMap<String, Object>>>();
        for (CustBillHist custBillDet : custBill) {
            final Map<String, Object> billDet = new  HashMap<String, Object>();
            final List<Map<String, Object>> lunchList = new  ArrayList<Map<String, Object>>();
            final List<Map<String, Object>> dinnerList = new  ArrayList<Map<String, Object>>();
            final List<Map<String, Object>> breakFastList = new  ArrayList<Map<String, Object>>();
            final List<Map<String, Object>> snacksList = new  ArrayList<Map<String, Object>>();
            List<CustOrdrs> custOrd = cust.getOrderDetailsByMealTime(custBillDet.getId());
            for (CustOrdrs ord : custOrd) {
                List<CustOrdDet> custOrders = cust.getOrderDetailsByOrderNo(ord.getOrdNo());
                for (CustOrdDet orderDet : custOrders) {
                    final Map<String, Object> lunchOrderDetails = new  HashMap<String, Object>();
                    final Map<String, Object> dinnerOrderDetails = new  HashMap<String, Object>();
                    final Map<String, Object> breakFastOrderDetails = new  HashMap<String, Object>();
                    final Map<String, Object> snacksOrderDetails = new  HashMap<String, Object>();
                    // System.out.println("values>>"+orderDet.getPkgSize().getId());
                    if (orderDet.getMealTime().getId() == 1) {
                        lunchOrderDetails.put("pkgId", orderDet.getPkgSize().getId());
                        lunchOrderDetails.put("orderId", orderDet.getId());
                        lunchOrderDetails.put("pkgName", orderDet.getPkgSize().getPkgSizTypDesc());
                        lunchOrderDetails.put("pkgDurDescription", orderDet.getPkgDur().getPkgDurDesc());
                        lunchOrderDetails.put("weekTypDesc", orderDet.getWeekTyp().getWeekTypDesc());
                        lunchOrderDetails.put("pkgQuantity", orderDet.getPkgSizTypQty());
                        lunchOrderDetails.put("pkgAmount", orderDet.getOrdBillAmnt());
                        lunchOrderDetails.put("orderStartDate", orderDet.getOrdStDt());
                        lunchOrderDetails.put("orderEndDate", orderDet.getOrdEndDt());
                        lunchList.add(lunchOrderDetails);
                    }
                    if (orderDet.getMealTime().getId() == 2) {
                        dinnerOrderDetails.put("pkgId", orderDet.getPkgSize().getId());
                        dinnerOrderDetails.put("orderId", orderDet.getId());
                        dinnerOrderDetails.put("pkgName", orderDet.getPkgSize().getPkgSizTypDesc());
                        dinnerOrderDetails.put("pkgDurDescription", orderDet.getPkgDur().getPkgDurDesc());
                        dinnerOrderDetails.put("weekTypDesc", orderDet.getWeekTyp().getWeekTypDesc());
                        dinnerOrderDetails.put("pkgQuantity", orderDet.getPkgSizTypQty());
                        dinnerOrderDetails.put("pkgAmount", orderDet.getOrdBillAmnt());
                        dinnerOrderDetails.put("orderStartDate", orderDet.getOrdStDt());
                        dinnerOrderDetails.put("orderEndDate", orderDet.getOrdEndDt());
                        dinnerList.add(dinnerOrderDetails);
                    }
                    if (orderDet.getMealTime().getId() == 3) {
                        breakFastOrderDetails.put("pkgId", orderDet.getPkgSize().getId());
                        breakFastOrderDetails.put("orderId", orderDet.getId());
                        breakFastOrderDetails.put("pkgName", orderDet.getPkgSize().getPkgSizTypDesc());
                        breakFastOrderDetails.put("pkgDurDescription", orderDet.getPkgDur().getPkgDurDesc());
                        breakFastOrderDetails.put("weekTypDesc", orderDet.getWeekTyp().getWeekTypDesc());
                        breakFastOrderDetails.put("pkgQuantity", orderDet.getPkgSizTypQty());
                        breakFastOrderDetails.put("pkgAmount", orderDet.getOrdBillAmnt());
                        breakFastOrderDetails.put("orderStartDate", orderDet.getOrdStDt());
                        breakFastOrderDetails.put("orderEndDate", orderDet.getOrdEndDt());
                        breakFastList.add(breakFastOrderDetails);
                    }
                    if (orderDet.getMealTime().getId() == 4) {
                        snacksOrderDetails.put("pkgId", orderDet.getPkgSize().getId());
                        snacksOrderDetails.put("orderId", orderDet.getId());
                        snacksOrderDetails.put("pkgName", orderDet.getPkgSize().getPkgSizTypDesc());
                        snacksOrderDetails.put("pkgDurDescription", orderDet.getPkgDur().getPkgDurDesc());
                        snacksOrderDetails.put("weekTypDesc", orderDet.getWeekTyp().getWeekTypDesc());
                        snacksOrderDetails.put("pkgQuantity", orderDet.getPkgSizTypQty());
                        snacksOrderDetails.put("pkgAmount", orderDet.getOrdBillAmnt());
                        snacksOrderDetails.put("orderStartDate", orderDet.getOrdStDt());
                        snacksOrderDetails.put("orderEndDate", orderDet.getOrdEndDt());
                        snacksList.add(snacksOrderDetails);
                    }
                }
            }
            billDet.put("billId", custBillDet.getId());
            // billDet.put("orderEndDate",custBillDet.getOrdEndDt());
            billDet.put("subAmount", custBillDet.getSubAmnt());
            billDet.put("grandTotal", custBillDet.getGrndTot());
            billDet.put("subGrandTotal", custBillDet.getSubGrndTot());
            billDet.put("cupnDiscAmnt", custBillDet.getCupnDiscAmnt());
            billDet.put("redeemCancelAmnt", custBillDet.getRedeemCancelAmnt());
            billDet.put("deliveryChrges", custBillDet.getDelChrg());
            billDet.put("taxAmount", custBillDet.getTaxAmnt());
            billDet.put("lunchOrdersList", lunchList);
            billDet.put("dinnerOrdersList", dinnerList);
            billDet.put("breakFastOrdersList", breakFastList);
            billDet.put("snacksOrdersList", snacksList);
            billWiseList.add(billDet);
        }
        if (billWiseList.size() > 0) {
            map.put("billWiseList", billWiseList);
        } else {
        }
        return map;
    }

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

    public void sendEmail(String emailAddress, String emailBody) {
        final String FROM = "info@spicebox.in";
        /*String BODY = "Hi " + order.getCustDet().getCustName() + ",\n\n"
				+ "We are glad to have a Starfood Customer like you! \n\n"
				+ "Your order #" + order.getId() + " for "
				+ order.getPkgSizTypQty() + " quantity, "
				//+ order.getMealTyp().getMealTypDesc() + ", "
				+ order.getFoodTyp().getFoodTypeDesc() +","
				+ order.getMealTime().getMealTimeDesc()+","
				+ order.getPkgDur().getPkgDurDesc()+
				" Meal has been placed.\n\n"
				+ "Your delicious Starfood Meal Plan begins from "
				+ order.getOrdStDt()
				+ ".\n\n Hope you love the food!\n Happy Eating! :)\n\n"
				+ "Team StarFood!";*/
        final String TO = emailAddress;
        String BODY = emailBody;
        final String SUBJECT = "Starfood Meal Delivery";
        // gurmit
        final String SMTP_USERNAME = "AKIAINVWQY4OC4JYLQTQ";
        // gurmit
        final String SMTP_PASSWORD = "AvQ5uT1ufPbxMoAhGeSk2SFRwIrNOKRueDoEVO9Y63j+";
        final String HOST = "email-smtp.us-west-2.amazonaws.com";
        final int PORT = 25;
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.setProperty("mail.smtp.host", HOST);
        props.setProperty("mail.smtps.auth", "true");
        props.setProperty("mail.user", "info@spicebox.in");
        props.setProperty("mail.password", "macintosh1");
        props.setProperty("mail.debug", "true");
        Session session = Session.getDefaultInstance(props);
        MimeMessage msg = new  MimeMessage(session);
        try {
            msg.setFrom(new  InternetAddress(FROM));
            msg.setRecipient(Message.RecipientType.TO, new  InternetAddress(TO));
            msg.setSubject(SUBJECT);
            msg.setContent(BODY, "text/plain");
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        SMTPTransport smtpTransport = null;
        try {
            smtpTransport = new  SMTPTransport(session, new  URLName("email-smtp.us-west-2.amazonaws.com"));
            System.out.println("Attempting to send an email through the Amazon SES SMTP interface...");
            smtpTransport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);
            smtpTransport.sendMessage(msg, msg.getAllRecipients());
            System.out.println("Email sent!");
        } catch (Exception ex) {
            System.out.println("The email was not sent.");
            System.out.println("Error message: " + ex.getMessage());
        } finally {
            try {
                smtpTransport.close();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    public Date getTime() {
        DateFormat df = new  SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa");
        DateFormat outputformat = new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

    public Calendar getIndianTimezoneCalendar() {
        Calendar cSchedStartCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        long gmtTime = cSchedStartCal.getTime().getTime();
        long timezoneAlteredTime = gmtTime + TimeZone.getTimeZone("Asia/Calcutta").getRawOffset();
        Calendar cSchedStartCal1 = Calendar.getInstance(TimeZone.getTimeZone("Asia/Calcutta"));
        cSchedStartCal1.setTimeInMillis(timezoneAlteredTime);
        return cSchedStartCal1;
    }

    public Date getIndianTimeZoneWithDate() {
        DateFormat df = new  SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa");
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
        return outputDate;
    }

    public String getIndianTimeWithStringForMealTime() {
        DateFormat df = new  SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa");
        DateFormat outputformat = new  SimpleDateFormat("hh:mm a");
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
        return sbCurrentTimestamp;
    }
}
