package org.example.report;

import java.util.List;

public record TechnicalDebtReport(List<TechnicalDebtItem> items) {
    public TechnicalDebtReport {
        items = List.copyOf(items);
    }

    public long totalMinutes() {
        return items.stream().mapToLong(TechnicalDebtItem::estimatedMinutes).sum();
    }

    public String format() {
        StringBuilder result = new StringBuilder("Technical debt estimate\n");
        result.append("Items: ").append(items.size()).append('\n');
        result.append("Estimated effort (minutes): ").append(totalMinutes()).append('\n');
        for (TechnicalDebtItem item : items) {
            result.append(item.fileName()).append(": ").append(item.reason())
                    .append(" (+").append(item.estimatedMinutes()).append(" min)\n");
        }
        return result.toString();
    }
}
