package net.sleeppunisher.utils;

public class Probability {
    static Integer findCeil(Integer arr[], Integer r, Integer l, Integer h) {
        Integer mid;

        while (l < h) {
            mid = l + ((h - l) >> 1);
            if (r > arr[mid])
                l = mid + 1;
            else
                h = mid;
        }

        return (arr[l] >= r) ? l : -1;
    }

    public static Integer randNum(Integer arr[], Integer freq[], Integer n) {
        Integer prefix[] = new Integer[n], i;
        prefix[0] = freq[0];

        for (i = 1; i < n; ++i)
            prefix[i] = prefix[i - 1] + freq[i];

        int r = ((int) (Math.random() * (323567)) % prefix[n - 1]) + 1;

        Integer indexc = findCeil(prefix, r, 0, n - 1);

        return arr[indexc];
    }
}
