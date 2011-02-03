package wyjs.testing.tests;

import org.junit.Ignore;
import org.junit.Test;

import wyjs.testing.TestHarness;

public class StaticInvalidTests extends TestHarness {

  public StaticInvalidTests() {
    super("tests/invalid", "tests/invalid", "sysout");
  }

  @Test
  public void Assert_CompileFail_1_StaticTest() {
    contextFailTest("Assert_CompileFail_1");
  }

  @Test
  public void Assign_CompileFail_1_StaticTest() {
    contextFailTest("Assign_CompileFail_1");
  }

  @Test
  public void Assign_CompileFail_2_StaticTest() {
    contextFailTest("Assign_CompileFail_2");
  }

  @Test
  public void Assign_CompileFail_3_StaticTest() {
    contextFailTest("Assign_CompileFail_3");
  }

  @Test
  public void Assign_CompileFail_4_StaticTest() {
    contextFailTest("Assign_CompileFail_4");
  }

  @Test
  public void Assign_CompileFail_5_StaticTest() {
    contextFailTest("Assign_CompileFail_5");
  }

  @Test
  public void Assign_CompileFail_6_StaticTest() {
    contextFailTest("Assign_CompileFail_6");
  }

  @Test
  public void Assign_CompileFail_7_StaticTest() {
    contextFailTest("Assign_CompileFail_7");
  }

  @Test
  public void DefiniteAssign_CompileFail_4_StaticTest() {
    contextFailTest("DefiniteAssign_CompileFail_4");
  }

  @Test
  public void For_CompileFail_1_StaticTest() {
    contextFailTest("For_CompileFail_1");
  }

  @Test
  public void Function_CompileFail_1_StaticTest() {
    contextFailTest("Function_CompileFail_1");
  }

  @Ignore("Known Bug")
  @Test
  public void Function_CompileFail_2_StaticTest() {
    contextFailTest("Function_CompileFail_2");
  }

  @Test
  public void Function_CompileFail_3_StaticTest() {
    contextFailTest("Function_CompileFail_3");
  }

  @Test
  public void Function_CompileFail_4_StaticTest() {
    contextFailTest("Function_CompileFail_4");
  }

  @Test
  public void If_CompileFail_1_StaticTest() {
    contextFailTest("If_CompileFail_1");
  }

  @Test
  public void If_CompileFail_2_StaticTest() {
    contextFailTest("If_CompileFail_2");
  }

  @Test
  public void If_CompileFail_3_StaticTest() {
    contextFailTest("If_CompileFail_3");
  }

  @Test
  public void If_CompileFail_4_StaticTest() {
    contextFailTest("If_CompileFail_4");
  }

  @Test
  public void ListAccess_CompileFail_1_StaticTest() {
    contextFailTest("ListAccess_CompileFail_1");
  }

  @Test
  public void ListAccess_CompileFail_3_StaticTest() {
    contextFailTest("ListAccess_CompileFail_3");
  }

  @Test
  public void ListAppend_Invalid_1_StaticTest() {
    contextFailTest("ListAppend_Invalid_1");
  }

  @Test
  public void ListAppend_Invalid_2_StaticTest() {
    contextFailTest("ListAppend_Invalid_2");
  }

  @Test
  public void ListAssign_CompileFail_1_StaticTest() {
    contextFailTest("ListAssign_CompileFail_1");
  }

  @Test
  public void ListConversion_CompileFail_1_StaticTest() {
    contextFailTest("ListConversion_CompileFail_1");
  }

  @Test
  public void ListDefine_CompileFail_1_StaticTest() {
    contextFailTest("ListDefine_CompileFail_1");
  }

  @Test
  public void ListElemOf_CompileFail_1_StaticTest() {
    contextFailTest("ListElemOf_CompileFail_1");
  }

  @Test
  public void ListEmpty_CompileFail_1_StaticTest() {
    contextFailTest("ListEmpty_CompileFail_1");
  }

  @Test
  public void ListSublist_CompileFail_1_StaticTest() {
    contextFailTest("ListSublist_CompileFail_1");
  }

  @Test
  public void ListSublist_CompileFail_3_StaticTest() {
    contextFailTest("ListSublist_CompileFail_3");
  }

  @Test
  public void List_CompileFail_1_StaticTest() {
    contextFailTest("List_CompileFail_1");
  }

  @Test
  public void List_CompileFail_2_StaticTest() {
    contextFailTest("List_CompileFail_2");
  }

  @Test
  public void List_CompileFail_3_StaticTest() {
    contextFailTest("List_CompileFail_3");
  }

  @Test
  public void List_CompileFail_4_StaticTest() {
    contextFailTest("List_CompileFail_4");
  }

  @Test
  public void List_CompileFail_5_StaticTest() {
    contextFailTest("List_CompileFail_5");
  }

  @Test
  public void List_CompileFail_6_StaticTest() {
    contextFailTest("List_CompileFail_6");
  }

  @Test
  public void MethodCall_CompileFail_1_StaticTest() {
    contextFailTest("MethodCall_CompileFail_1");
  }

  @Test
  public void MethodCall_CompileFail_2_StaticTest() {
    contextFailTest("MethodCall_CompileFail_2");
  }

  @Test
  public void MethodCall_CompileFail_3_StaticTest() {
    contextFailTest("MethodCall_CompileFail_3");
  }

  @Test
  public void MethodCall_CompileFail_4_StaticTest() {
    contextFailTest("MethodCall_CompileFail_4");
  }

  @Test
  public void MethodCall_CompileFail_5_StaticTest() {
    contextFailTest("MethodCall_CompileFail_5");
  }

  @Test
  public void MethodCall_CompileFail_6_StaticTest() {
    contextFailTest("MethodCall_CompileFail_6");
  }

  @Test
  public void MethodCall_CompileFail_7_StaticTest() {
    contextFailTest("MethodCall_CompileFail_7");
  }

  @Test
  public void MethodCall_CompileFail_8_StaticTest() {
    contextFailTest("MethodCall_CompileFail_8");
  }

  @Test
  public void ProcessAccess_CompileFail_1_StaticTest() {
    contextFailTest("ProcessAccess_CompileFail_1");
  }

  @Test
  public void ProcessAccess_CompileFail_2_StaticTest() {
    contextFailTest("ProcessAccess_CompileFail_2");
  }

  @Test
  public void RealAdd_CompileFail_1_StaticTest() {
    contextFailTest("RealAdd_CompileFail_1");
  }

  @Test
  public void RealDiv_CompileFail_1_StaticTest() {
    contextFailTest("RealDiv_CompileFail_1");
  }

  @Test
  public void RecursiveType_Invalid_4_StaticTest() {
    contextFailTest("RecursiveType_Invalid_4");
  }

  @Test
  public void RecursiveType_Invalid_5_StaticTest() {
    contextFailTest("RecursiveType_Invalid_5");
  }

  @Test
  public void Return_CompileFail_1_StaticTest() {
    contextFailTest("Return_CompileFail_1");
  }

  @Test
  public void Return_CompileFail_10_StaticTest() {
    contextFailTest("Return_CompileFail_10");
  }

  @Test
  public void Return_CompileFail_11_StaticTest() {
    contextFailTest("Return_CompileFail_11");
  }

  @Test
  public void Return_CompileFail_2_StaticTest() {
    contextFailTest("Return_CompileFail_2");
  }

  @Test
  public void Return_CompileFail_3_StaticTest() {
    contextFailTest("Return_CompileFail_3");
  }

  @Test
  public void Return_CompileFail_4_StaticTest() {
    contextFailTest("Return_CompileFail_4");
  }

  @Test
  public void Return_CompileFail_5_StaticTest() {
    contextFailTest("Return_CompileFail_5");
  }

  @Test
  public void Return_CompileFail_6_StaticTest() {
    contextFailTest("Return_CompileFail_6");
  }

  @Test
  public void Return_CompileFail_7_StaticTest() {
    contextFailTest("Return_CompileFail_7");
  }

  @Test
  public void Return_CompileFail_8_StaticTest() {
    contextFailTest("Return_CompileFail_8");
  }

  @Test
  public void Return_CompileFail_9_StaticTest() {
    contextFailTest("Return_CompileFail_9");
  }

  @Test
  public void SetComprehension_CompileFail_1_StaticTest() {
    contextFailTest("SetComprehension_CompileFail_1");
  }

  @Test
  public void SetComprehension_CompileFail_2_StaticTest() {
    contextFailTest("SetComprehension_CompileFail_2");
  }

  @Test
  public void SetComprehension_CompileFail_3_StaticTest() {
    contextFailTest("SetComprehension_CompileFail_3");
  }

  @Test
  public void SetComprehension_CompileFail_4_StaticTest() {
    contextFailTest("SetComprehension_CompileFail_4");
  }

  @Test
  public void SetComprehension_CompileFail_5_StaticTest() {
    contextFailTest("SetComprehension_CompileFail_5");
  }

  @Test
  public void SetConversion_CompileFail_1_StaticTest() {
    contextFailTest("SetConversion_CompileFail_1");
  }

  @Test
  public void SetDefine_CompileFail_1_StaticTest() {
    contextFailTest("SetDefine_CompileFail_1");
  }

  @Test
  public void SetDefine_CompileFail_2_StaticTest() {
    contextFailTest("SetDefine_CompileFail_2");
  }

  @Test
  public void SetElemOf_CompileFail_1_StaticTest() {
    contextFailTest("SetElemOf_CompileFail_1");
  }

  @Test
  public void SetEmpty_CompileFail_1_StaticTest() {
    contextFailTest("SetEmpty_CompileFail_1");
  }

  @Test
  public void SetIntersect_CompileFail_1_StaticTest() {
    contextFailTest("SetIntersect_CompileFail_1");
  }

  @Test
  public void SetIntersect_CompileFail_2_StaticTest() {
    contextFailTest("SetIntersect_CompileFail_2");
  }

  @Test
  public void SetUnion_CompileFail_1_StaticTest() {
    contextFailTest("SetUnion_CompileFail_1");
  }

  @Test
  public void SetUnion_CompileFail_2_StaticTest() {
    contextFailTest("SetUnion_CompileFail_2");
  }

  @Test
  public void TupleDefine_CompileFail_1_StaticTest() {
    contextFailTest("TupleDefine_CompileFail_1");
  }

  @Test
  public void TypeEquals_Invalid_1_StaticTest() {
    contextFailTest("TypeEquals_Invalid_1");
  }

  @Test
  public void UnionType_CompileFail_1_StaticTest() {
    contextFailTest("UnionType_CompileFail_1");
  }

  @Test
  public void UnionType_CompileFail_2_StaticTest() {
    contextFailTest("UnionType_CompileFail_2");
  }

  @Test
  public void UnionType_CompileFail_3_StaticTest() {
    contextFailTest("UnionType_CompileFail_3");
  }

  @Test
  public void UnionType_CompileFail_4_StaticTest() {
    contextFailTest("UnionType_CompileFail_4");
  }

  @Test
  public void UnionType_CompileFail_5_StaticTest() {
    contextFailTest("UnionType_CompileFail_5");
  }

  @Test
  public void UnionType_CompileFail_6_StaticTest() {
    contextFailTest("UnionType_CompileFail_6");
  }

  @Test
  public void VarDecl_CompileFail_4_StaticTest() {
    contextFailTest("VarDecl_CompileFail_4");
  }

  @Test
  public void Void_CompileFail_1_StaticTest() {
    contextFailTest("Void_CompileFail_1");
  }

  @Test
  public void Void_CompileFail_2_StaticTest() {
    contextFailTest("Void_CompileFail_2");
  }
  
}
