# EventBus
事件总线

#简介
事件总线的思路源于 https://github.com/greenrobot/EventBus  
不过代码和实现方式完全不同于EventBus.

greenrobot的EventBus是通过 onEvent的方式，然后定义Event实体类  

而本项目的代码是通过用户定义IEvent接口，然后apt实现接口负责分发调用注册者的方法，使用者通过这个实现类去调用接口的方法，直接通知注册者

#EventBus in 3 steps

1.定义事件接口，直接继承于IEvent，添加 @Event 注解（用于apt自动生成事件分发实现类）  
	
	@Event
    public interface IMessageEvent extends IEvent{ 
	    	void onReceiveMessage(String message);
	 }

2.注册监听，实现事件接口

	 public MainActivity extends Activity implements IMessageEvent{

	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
			//注册监听
			EventBus.register(this);
		}

	    @Override
	    protected void onDestroy() {
	        super.onDestroy();
			//注销监听
			EventBus.unregister(this);
	    }

		@Override
		public void onReceiveMessage(String message){
	       
	    }
	 }

3.发送事件

	EventBus.get(IMessageEvent.class).onReceiveMessage("Message");

##主力类库##

**1.https://github.com/LuckyJayce/ViewPagerIndicator**  
Indicator 取代 tabhost，实现网易顶部tab，新浪微博主页底部tab，引导页，无限轮播banner等效果，高度自定义tab和特效

**2.https://github.com/LuckyJayce/MVCHelper**  
实现下拉刷新，滚动底部自动加载更多，分页加载，自动切换显示网络失败布局，暂无数据布局，支持任意view，支持切换主流下拉刷新框架。

**3.https://github.com/LuckyJayce/MultiTypeView**  
简化RecyclerView的多种type的adapter，Fragment可以动态添加到RecyclerView上，实现复杂的界面分多个模块开发

**4.https://github.com/LuckyJayce/EventBus**  
事件总线，通过动态代理接口的形式发布,接收事件。定义一个接口把事件发给注册并实现接口的类

**5.https://github.com/LuckyJayce/EventBus-Apt**  
事件总线，通过Apt实现接口分发事件。定义一个接口把事件发给注册并实现接口的类

**6.https://github.com/LuckyJayce/LargeImage**  
大图加载，可供学习

**7.https://github.com/LuckyJayce/GuideHelper**  
新手引导页，轻松的实现对应的view上面的显示提示信息和展示功能给用户  

**8.https://github.com/LuckyJayce/HVScrollView**  
可以双向滚动的ScrollView，支持嵌套ScrollView联级滑动，支持设置支持的滚动方向

**9.https://github.com/LuckyJayce/CoolRefreshView**  
  下拉刷新RefreshView，支持任意View的刷新 ，支持自定义Header，支持NestedScrollingParent,NestedScrollingChild的事件分发，嵌套ViewPager不会有事件冲突 

有了这些类库，让你6的飞起

#License

	Copyright 2017 LuckyJayce
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	   http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
