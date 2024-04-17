### 此文件夹中的内容是有关于TGD的测试用例

- test1:2023.12 编写的基本的TGD的测试用例(限制：①对于任何一个bodyAtom，其内部的 variable都不能重复出现，否则无法处理；②对于任何一个headAtom，其内部的 variable都不能重复出现，否则运行的结果与预期不一致)

  无法处理：

  - ① R(x0,x1,x0) -> P(x0,z)
  - ② R(x,y,z) -> R(a,a,x)

- test2：用来测试为了处理上述无法处理的第①种形式添加逻辑后的代码  

- test3：用来测试为了处理上述无法处理的第②种形式添加逻辑后的代码

- test4：用来制作中期答辩PPT所给出的示例设计的测试用例

- test5：之前测试的TGD中，headAtoms中的原子之间没有包含重复的Variable。实现的TGD处理中存在bug，对于headAtoms中的原子之间包含重复Variable的TGD处理结果不正确。此测试用例为修复该bug的测试，也是说明standard chase和parallel chase之间区别的测试用例。