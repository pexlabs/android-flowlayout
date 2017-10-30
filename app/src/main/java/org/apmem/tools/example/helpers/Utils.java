package org.apmem.tools.example.helpers;

import org.apmem.tools.model.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kaustubh on 30/10/17.
 */

public class Utils {

    /**
     * returns the Dummy data for demo purpose
     * @return
     */
    public static List<Chip> getDummyData() {
        List<Chip> data = new ArrayList<>();
        data.add(new Chip("Kaustubh", "kaustubh@astro-inc.com"));
        data.add(new Chip("Anthony Lee", "anthony@astro-inc.com"));
        data.add(new Chip("Ian", "ian@astro-inc.com"));
        data.add(new Chip("Andy F", "andy@astro-inc"));
        data.add(new Chip("San Wai", "san@astro-inc.com"));
        data.add(new Chip("Roland", "roland@astro-inc.com"));
        data.add(new Chip("Omkar T", "omkar@astro-inc.com"));
        data.add(new Chip("Faisal", "faisal@astro-inc.com"));
        return data;
    }
}
