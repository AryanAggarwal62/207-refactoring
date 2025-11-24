package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private Invoice invoice;
    private Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public Map<String, Play> getPlays() {
        return plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        final int totalAmount = getTotalAmount();
        final int volumeCredits = getTotalVolumeCredits();
        final StringBuilder result = new StringBuilder("Statement for "
                + this.invoice.getCustomer() + System.lineSeparator());

        for (Performance performance : this.invoice.getPerformances()) {
            final Play play = getPlay(performance);
            // print line for this order
            result.append(String.format("  %s: %s (%s seats)%n", play.getName(),
                    usd(getAmount(performance)), performance.getAudience()));
        }
        result.append(String.format("Amount owed is %s%n", usd(totalAmount)));
        result.append(String.format("You earned %s credits%n", volumeCredits));
        return result.toString();
    }

    private Play getPlay(Performance performance) {
        return this.plays.get(performance.getPlayID());
    }

    private int getAmount(Performance performance) {
        final Play play = getPlay(performance);
        int result = 0;
        switch (play.getType()) {
            case "tragedy":
                result = Constants.TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;
            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;
            case "history":
                result = Constants.HISTORY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.HISTORY_AUDIENCE_THRESHOLD) {
                    result += Constants.HISTORY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.HISTORY_AUDIENCE_THRESHOLD);
                }
                break;
            case "pastoral":
                result = Constants.PASTORAL_BASE_AMOUNT;
                if (performance.getAudience() > Constants.PASTORAL_AUDIENCE_THRESHOLD) {
                    result += Constants.PASTORAL_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.PASTORAL_AUDIENCE_THRESHOLD);
                }
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", play.getType()));
        }
        return result;
    }

    private int getVolumeCredits(Performance performance) {
        final Play play = getPlay(performance);
        int result = 0;
        if ("history".equals(play.getType())) {
            result = Math.max(performance.getAudience() - Constants.HISTORY_VOLUME_CREDIT_THRESHOLD, 0);
        }
        else if ("pastoral".equals(play.getType())) {
            result = Math.max(performance.getAudience() - Constants.PASTORAL_VOLUME_CREDIT_THRESHOLD, 0)
                    + performance.getAudience() / Constants.PASTORAL_VOLUME_CREDIT_DIVISOR;
        }
        else {
            result = Math.max(performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
            // add extra credit for every five comedy attendees
            if ("comedy".equals(play.getType())) {
                result += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
            }
        }
        return result;
    }

    private String usd(int amount) {
        return NumberFormat.getCurrencyInstance(Locale.US)
                .format(amount / Constants.PERCENT_FACTOR);
    }

    private int getTotalAmount() {
        int result = 0;
        for (Performance performance : this.invoice.getPerformances()) {
            result += getAmount(performance);
        }
        return result;
    }

    private int getTotalVolumeCredits() {
        int result = 0;
        for (Performance performance : this.invoice.getPerformances()) {
            result += getVolumeCredits(performance);
        }
        return result;
    }
}
