package io.telepat.sdk.models;

/**
 * Created by Andrei on 07.03.2016.
 *
 */
public class SubscriptionSorter {
    public enum SortDirection {
        ascending {
            public String toString() { return "asc"; }
        },
        descending {
            public String toString() { return "desc"; }
        }
    }

    public SubscriptionSorter(String sortingField, SortDirection sortingDirection) {
        this.sortingField = sortingField;
        this.sortingDirection = sortingDirection;
    }

    private String sortingField;
    private SortDirection sortingDirection;

    public String getSortingField() {
        return sortingField;
    }

    @SuppressWarnings("unused")
    public void setSortingField(String sortingField) {
        this.sortingField = sortingField;
    }

    public SortDirection getSortingDirection() {
        return sortingDirection;
    }

    @SuppressWarnings("unused")
    public void setSortingDirection(SortDirection sortingDirection) {
        this.sortingDirection = sortingDirection;
    }
}
