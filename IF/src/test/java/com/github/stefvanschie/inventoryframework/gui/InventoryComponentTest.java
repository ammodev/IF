package com.github.stefvanschie.inventoryframework.gui;

import com.github.stefvanschie.inventoryframework.pane.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryComponentTest {

  @Test
  void testConstructor() {
    assertThrows(IllegalArgumentException.class, () -> new InventoryComponent(-1, 1));
    assertThrows(IllegalArgumentException.class, () -> new InventoryComponent(1, -1));
    assertThrows(IllegalArgumentException.class, () -> new InventoryComponent(-1, -1));
    assertDoesNotThrow(() -> new InventoryComponent(0, 0));
  }

  @Test
  void testAddPane() {
    InventoryComponent inventoryComponent = new InventoryComponent(0, 0);

    inventoryComponent.addPane(new StaticPane(1, 1));

    List<Pane> panes = inventoryComponent.panes;

    assertEquals(1, panes.size());
    assertTrue(panes.get(0) instanceof StaticPane);
  }

  @Test
  void testCopy() {
    InventoryComponent original = new InventoryComponent(0, 0);

    original.addPane(new StaticPane(1, 1));
    original.addPane(new OutlinePane(1, 1));

    InventoryComponent copy = original.copy();

    assertNotSame(original, copy);

    assertEquals(original.length, copy.length);
    assertEquals(original.height, copy.height);
    assertEquals(original.panes.size(), copy.panes.size());
  }

  @Test
  void testExcludeRowsValid() {
    InventoryComponent original = new InventoryComponent(0, 6);

    original.addPane(new StaticPane(1, 1));
    original.addPane(new OutlinePane(1, 1));
    original.addPane(new PaginatedPane(1, 1));
    original.addPane(new MasonryPane(1, 1));

    InventoryComponent shrunk = original.excludeRows(4, 4);

    assertEquals(5, shrunk.height);
    assertEquals(original.panes.size(), shrunk.panes.size());

    for (Pane pane : original.panes) {
      assertTrue(shrunk.panes.contains(pane));
    }
  }

  @Test
  void testExcludeRowsInvalid() {
    InventoryComponent inventoryComponent = new InventoryComponent(0, 5);

    //noinspection ResultOfMethodCallIgnored
    assertThrows(IllegalArgumentException.class, () -> inventoryComponent.excludeRows(8, 8));
  }

  @Test
  void testGetPanesEmptyWhenNone() {
    assertEquals(0, new InventoryComponent(0, 0).panes.size());
  }

  @Test
  void testGetPanesSorted() {
    InventoryComponent inventoryComponent = new InventoryComponent(0, 0);

    inventoryComponent.addPane(new StaticPane(0, 0, 1, 1, Pane.Priority.HIGHEST));
    inventoryComponent.addPane(new OutlinePane(0, 0, 1, 1, Pane.Priority.LOW));
    inventoryComponent.addPane(new PaginatedPane(0, 0, 1, 1, Pane.Priority.MONITOR));

    List<Pane> panes = inventoryComponent.panes;

    assertEquals(Pane.Priority.LOW, panes.get(0).priority);
    assertEquals(Pane.Priority.HIGHEST, panes.get(1).priority);
    assertEquals(Pane.Priority.MONITOR, panes.get(2).priority);
  }

  @Test
  void testGetSize() {
    assertEquals(30, new InventoryComponent(3, 10).getSize());
  }
}
