package fr.myapplication.dc.myapplication.data.factory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import fr.myapplication.dc.myapplication.data.model.BaseMessage;
import fr.myapplication.dc.myapplication.data.model.Contact;
import fr.myapplication.dc.myapplication.data.model.Invitation;
import fr.myapplication.dc.myapplication.data.model.VibrationMessage;
import fr.myapplication.dc.myapplication.data.model.User;
import util.LoggerHelper;

/**
 * Created by Crono on 27/11/16.
 */

public class DataFactory {

    public static User createUser(final String login, final String email, final String token,
                                  final String phone){

        User user = new User();

        user.setPhone(phone);
        user.setLogin(login);
        user.setEmail(email);
        user.setRegid(token);

        return user;
    }


    public static Contact createContact(final String login, final boolean active){

        Contact contact = new Contact();

        contact.setLogin(login);
        contact.setActive(active);
        contact.initMessageList();

        return contact;
    }

    public static Invitation createInvitation(final String login){

        Invitation invitation = new Invitation();

        invitation.setLogin(login);

        return invitation;
    }

    public static BaseMessage createMessage(final String from,
                                            final String to){


        BaseMessage msg = new BaseMessage();
        msg.setFrom(from);
        msg.setTo(to);

        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();
        int mGMTOffset = mTimeZone.getRawOffset();
        System.out.printf("GMT offset is %s hours", TimeUnit.HOURS.convert(mGMTOffset, TimeUnit.MILLISECONDS));

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        LoggerHelper.info("DataFactory","zoneOffset = " + zoneOffset);

        String timezoneStr = new SimpleDateFormat("Z").format( cal.getTime() );
        LoggerHelper.info("DataFactory","timezoneStr = " + timezoneStr);

        msg.setZone(timezoneStr);

        return msg;
    }

    //unused
    @Deprecated
    public static VibrationMessage createMessage(final VibrationMessage message, final int type, final String from,
                                                 final String to){

        LoggerHelper.debug("create message called with message=" + message.getPattern());

        VibrationMessage msg = new VibrationMessage();
        msg.setPattern(message.getPattern());
        msg.setFrom(from);
        msg.setTo(to);
        msg.setRead(false);//message not red yet
        //msg.setMessageType(type);
        //Date now = new Date();
        //msg.setFriend_since(now);

        return msg;
    }
}
