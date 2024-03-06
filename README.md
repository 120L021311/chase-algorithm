这个仓库原来的url：

``` bash
$ git remote -v
origin  git@github.com:120L021311/chase-algorithm.git (fetch)
origin  git@github.com:120L021311/chase-algorithm.git (push)
```

输入格式示例：

数据库实例数据：.csv文件，表头和属性值之间用","分隔；

TGD：R(x0,x1,x2) and Q(x3,x4,x5) -> P(x0,x1,x5) and S(x1,x2,x5) and T(x0,z)

EGD：R(a,b,c,d,e) and R(a,b1,c1,d1,e1) -> c=c1

​			R(a,b,c) and S(a,b1,c1) and T(c,c1) -> b=b1

example/test中是standard chase的正确性测试用例，其中只包含TGD和simple EGD；

example/test2中是区分oblivious chase、semi-oblivious chase和standard chase的简单用例