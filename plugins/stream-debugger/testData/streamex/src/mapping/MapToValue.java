package mapping;

import one.util.streamex.EntryStream;

public class MapToValue {
  public static void main(String[] args) {
    // Breakpoint!
    final long count = EntryStream.of(1, 1, 2, 4, 3, 9)
        .mapToValue((k, v) -> k + v)
        .count();
    System.out.println(count);
  }
}
