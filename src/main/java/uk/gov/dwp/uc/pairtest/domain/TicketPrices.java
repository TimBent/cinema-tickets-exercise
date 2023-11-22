package uk.gov.dwp.uc.pairtest.domain;

import java.math.BigDecimal;

final public class TicketPrices {

        private static final int INFANT_PRICE = 0;
        private static final int CHILD_PRICE = 10;
        private static final int ADULT_PRICE = 20;

        public static int getInfantPrice() {
            return INFANT_PRICE;
        }

        public static int getChildPrice() {
            return CHILD_PRICE;
        }

        public static int getAdultPrice() {
            return ADULT_PRICE;
        }

}
