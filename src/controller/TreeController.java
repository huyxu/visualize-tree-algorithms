package controller;

import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import model.BST;
import model.Node;
import model.RBTree;
import view.CircleNode;
import view.RBCircleNode;
import view.SearchCircleNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static model.Constants.*;

public class TreeController<T extends Comparable<T>> {
  public HashMap<Node, CircleNode> treeView;
  public BST tree;

  public TreeController(BST tree) {
    this.treeView = createTreeView(tree);
    this.tree = tree;
  }

  private HashMap<Node, CircleNode> createTreeView(BST tree) {
    HashMap<Node, CircleNode> treeView = new HashMap<>();
    if (tree.root != null) {
      if (tree instanceof RBTree) {
        createRBTreeView(treeView, tree.root, CENTER_X, CENTER_Y, H_GAP);
      } else {
        createTreeView(treeView, tree.root, CENTER_X, CENTER_Y, H_GAP);
      }
    }
    return treeView;
  }

  private void createTreeView(HashMap treeView, Node current, double centerX, double centerY, double hGap) {
    CircleNode cir = new CircleNode(current.element.toString(), centerX, centerY, hGap);

    if (current.left != null) {
      cir.setLineLeft(new Line(cir.getLayoutX() + 20, cir.getLayoutY() + 20, cir.getLayoutX() - cir.gethGap() + 20, cir.getLayoutY() + 50 + 20));
      createTreeView(treeView, current.left, centerX - hGap, centerY + V_GAP, hGap / 2);
    }

    if (current.right != null) {
      cir.setLineRight(new Line(cir.getLayoutX() + 20, cir.getLayoutY() + 20, cir.getLayoutX() + cir.gethGap() + 20, cir.getLayoutY() + 50 + 20));
      createTreeView(treeView, current.right, centerX + hGap, centerY + V_GAP, hGap / 2);
    }

    treeView.put(current, cir);
  }

  private void createRBTreeView(HashMap treeView, Node current, double centerX, double centerY, double hGap) {
    RBCircleNode cir = new RBCircleNode(current.element.toString(), centerX, centerY, hGap, current.color);

    if (current.left != null) {
      cir.setLineLeft(new Line(cir.getLayoutX() + 20, cir.getLayoutY() + 20, cir.getLayoutX() - cir.gethGap() + 20, cir.getLayoutY() + 50 + 20));
      createRBTreeView(treeView, current.left, centerX - hGap, centerY + V_GAP, hGap / 2);
    }

    if (current.right != null) {
      cir.setLineRight(new Line(cir.getLayoutX() + 20, cir.getLayoutY() + 20, cir.getLayoutX() + cir.gethGap() + 20, cir.getLayoutY() + 50 + 20));
      createRBTreeView(treeView, current.right, centerX + hGap, centerY + V_GAP, hGap / 2);
    }

    treeView.put(current, cir);
  }

  public void updateTreeView() {
    this.treeView = createTreeView(this.tree);
  }

  public void displayCircle(Group root) {
    this.treeView.forEach((node, cir) -> {
      root.getChildren().add(cir);
    });

  }

  public void displayLines(Group root) {
    this.treeView.forEach((node, cir) -> {
      if (cir.getLineLeft() != null) {
        cir.getLineLeft().setStroke(Color.rgb(49, 116, 222));
        cir.getLineLeft().setStrokeWidth(3);
        root.getChildren().add(cir.getLineLeft());
      }
      if (cir.getLineRight() != null) {
        cir.getLineRight().setStroke(Color.rgb(49, 116, 222));
        cir.getLineRight().setStrokeWidth(3);
        root.getChildren().add(cir.getLineRight());
      }
    });
  }

  public SequentialTransition createAnimationOnSearchTree(Group root, T element) {
    SequentialTransition sq = new SequentialTransition();
    if (this.tree.search(element) == null) {
      System.out.println(element.toString() + " not exists!");
      return null;
    } else {
      SearchCircleNode searchCir = new SearchCircleNode();
      searchCir.setLayoutX(500);
      searchCir.setLayoutY(50);
      double lastLayoutX = 500;
      for (CircleNode o : getSearchPath(element)) {
        if (o.getLayoutX() == 500) {
          TranslateTransition tr = searchCir.createAnimationTranslateTo(o.getLayoutX(), o.getLayoutY());
          sq.getChildren().add(tr);
        } else if (o.getLayoutX() >= lastLayoutX) {
          TranslateTransition tr = searchCir.createAnimationTranslateTo(500 + o.gethGap() * 2, 100);
          sq.getChildren().add(tr);
        } else {
          TranslateTransition tr = searchCir.createAnimationTranslateTo(500 - o.gethGap() * 2, 100);
          sq.getChildren().add(tr);
        }
        lastLayoutX = o.getLayoutX();
      }
      root.getChildren().add(searchCir);
      return sq;
    }
  }

  private ArrayList<CircleNode> getSearchPath(T element) {
    ArrayList<CircleNode> searchPath = new ArrayList<>();

    for (Object key : tree.path(element)) {
      searchPath.add(this.treeView.get(key));
    }
    return searchPath;
  }

  public ParallelTransition createAnimationHandleDelete(Group root, HashMap<Node, CircleNode> afterDeleteTreeView) {

    ParallelTransition pl = new ParallelTransition();

    this.treeView.forEach((bstKey, bstNode) -> {
      int check = 0; // if this node have in both tree -> 1; else -> 0 -> delete
      for (Map.Entry<Node, CircleNode> entry : afterDeleteTreeView.entrySet()) {
        Node avlKey = entry.getKey();
        CircleNode avlNode = entry.getValue();
        if (bstKey.element.compareTo(avlKey.element) == 0) {
          // this node represent in both tree
          check = 1;
          // Find position to moving to in avlTree
          double moveToX = avlNode.getLayoutX();
          double moveToY = avlNode.getLayoutY();
          // Add animation to root
          pl.getChildren().add(bstNode.createAnimationTranslateTo(moveToX, moveToY));
        }
      }

      // If not fine this node in AVL Tree -> remove it from screen
      if (check == 0) root.getChildren().remove(bstNode);
    });

    return pl;
  }

  public ParallelTransition createAnimationHandleInsert(Group root, HashMap<Node, CircleNode> afterInsertTreeView) {

    ParallelTransition pl = new ParallelTransition();

    this.treeView.forEach((bstKey, bstNode) -> {
      afterInsertTreeView.forEach((avlKey, avlNode) -> {
        if (bstKey.element.compareTo(avlKey.element) == 0) {
          // Find position to moving to in avlTree
          double moveToX = avlNode.getLayoutX();
          double moveToY = avlNode.getLayoutY();
          // Add animation to root
          pl.getChildren().add(bstNode.createAnimationTranslateTo(moveToX, moveToY));
        }
      });
    });

    return pl;
  }

  public void createTreeViewColor(RBTree rbTree) {
    HashMap<Node, CircleNode> treeViewColor = new HashMap<>();

    this.treeView.forEach((node, cir) -> {
      if (rbTree.search(node.element) != null) {
        RBCircleNode rbCir = new RBCircleNode(cir.value, cir.getLayoutX(), cir.getLayoutY(), cir.gethGap(), rbTree.search(node.element).color);
        rbCir.setLineLeft(cir.getLineLeft());
        rbCir.setLineRight(cir.getLineRight());
        treeViewColor.put(node, rbCir);
      }
    });

    this.treeView = treeViewColor;
  }
}
