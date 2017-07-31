/*$(function(){
	var user = $.cookie("user");
	var password = $.cookie("password");
	if(user && password){
		$("input[name='user']").val(user);
		$("input[name='password']").val(password);
		$("input[name='remember']").attr('checked',true);
	}
})*/

function login(){
	if(validate()){ 
		$('#form').submit();
	}
}
function validate(){
	var user = $("input[name='user']").val();
	var password = $("input[name='password']").val();
	var validateCode=$("input[name='validateCode']").val();
	
	if(user == "" || password == ""){
		$('#message').html('用户名密码不能为空');
		return;
	} else if(validateCode == "") {
		$('#message').html('验证码不能为空');
		return;
	}
	if($("input[name='remember']").is(':checked')){
		$.cookie("user",user,{expires:7});
		$.cookie("password",password,{expires:7});
	}else{
		if($.cookie("user") && $.cookie("password")){
			$.cookie("user",null,{expires:-1});
			$.cookie("password",null,{expires:-1}); 
		}
	}
	var flag = true;
	$.ajax({
		type : "POST",
		dataType : "json",
		async : false,
		url : "validate.action",
		data : {
			user : user,
			password : password,
			validateCode : validateCode
		},
		success : function(res) {
			if(res.code != 200){
				$('#message').html(res.msg);
				flag = false;
			}
		}
	});
	return flag;
}

function reset(){
	$('#form')[0].reset();
	$("input[name='remember']").attr('checked',false);
	if($.cookie("user") && $.cookie("password")){
		$.cookie("user",null,{expires:-1});
		$.cookie("password",null,{expires:-1}); 
	}
}

/*function logout(){
	$.ajax({
		type : "POST",
		dataType : "json",
		async : false,
		url : "logout.action",
		success : function(res) {
			if(res.code == 200){
				window.location ="login.html";
			}
		}
	});
}

function ShowElement(element) {
	$(element).removeAttr("ondblclick");
	var oldhtml = element.innerHTML; //获得元素之前的内容
	var newobj = document.createElement('input'); //创建一个input元素
	element.innerHTML = ''; //设置元素内容为空
	element.appendChild(newobj); //添加子元素
	newobj.type = 'text'; //为newobj元素添加类型
	$(newobj).attr("style","width:70%;");
	$(newobj).keydown(function(event) {
        if (event.keyCode == "13") {//keyCode=13是回车键
            $(this).blur();
        }
    });
    newobj.value = oldhtml;
	//设置newobj失去焦点的事件
	newobj.onblur = function() {
		$(element).attr("ondblclick","ShowElement(this)");
		element.innerHTML = this.value ? this.value : oldhtml; //当触发时判断newobj的值是否为空，为空则不修改，并返回oldhtml。
		if(this.value == oldhtml){
			return;
		}
		//组装json对象
		var name = $(element).attr("name");
		var roleId = $(element).parent().find("td:first").html();
		var inputJson = '{"'+name+'":"'+element.innerHTML+'"}';
		var dataJson =  eval("("+inputJson+")"); // eval（”(”+strJSON+”)”）
		dataJson.roleId = roleId;
		
		$.ajax({
			type : "POST",
			dataType : "json",
			async : false,
			url : "editRole.action",
			data : dataJson,
			success : function(res) {
				if(res.code != 200){
					window.location ="login.html";
				}
			}
		});
	};
	newobj.focus(); //获得焦点
}

//权限配置页面函数
function initUserManager(){
	var pageSize = 8;
	var pageCount = 1;
	$.ajax({
		type : "POST",
		dataType : "json",
		async : false,
		url : "getUsers.action",
		data:{
			page : 1,
			pageSize : pageSize
		},
		success : function(res) {
			if(res.code != 200){
				window.location ="login.html";
			}else{
				$("#personName").html("<span>"+res.user.name+"</span>");
				pageCount = res.total%pageSize==0 ? res.total/pageSize:res.total/pageSize+1;
				pageCount = Math.floor(pageCount);
				loadUsers(res.userList,pageSize);
			}
		}
	});
	 $(".pager").createPage({
	        pageCount:pageCount,
	        current:1,
	        backFn:function(p){
	        	$.ajax({
	        		type : "POST",
	        		dataType : "json",
	        		async : false,
	        		url : "getUsers.action",
	        		data:{
	        			page : p,
	        			pageSize : pageSize
	        		},
	        		success : function(res) {
	        			if(res.code != 200){
	        				window.location ="login.html";
	        			}else{
	        				$("table tr").filter("tr[name=row]").each(function(){ //filter("select[id=unOwn]")  
	        				    $(this).remove();
	        			        });
	        				loadUsers(res.userList,pageSize);
	        			}
	        		}
	        	});
	        }
	    });
}

function loadUsers(users,pageSize){
	$.each(users,function(index,user) {  
		var tbodyobj = document.createElement("tbody");
		var rowobj = document.createElement('tr'); //创建一个tr元素
		tbodyobj.appendChild(rowobj);
		rowobj.setAttribute("name","row");
		rowobj.style = "cursor: pointer";
		rowobj.id = "row"+user.id;
		var bgcolor = index%2==0 ? "#efefef":"#FFFFFF";
		rowobj.appendChild($(' <td height="24" name="id" bgcolor="'+bgcolor+'">'+user.id+'</td>')[0]);
		rowobj.appendChild($(' <td name="name" bgcolor="'+bgcolor+'" >'+user.name+'</td>')[0]);
		rowobj.appendChild($('<td bgcolor="'+bgcolor+'">'+user.cardnum+'</td>')[0]);
		rowobj.appendChild($('<td align="center" bgcolor="'+bgcolor+'">'+user.role+'</td>')[0]);
		rowobj.appendChild($('<td align="center" bgcolor="'+bgcolor+'"><a href="javascript:void(0)" onclick="editUserRole('+rowobj.id+')">修改</a></td>')[0]);
		document.getElementById('usertable').appendChild(tbodyobj);
	});  
//	alert($('#usertable').parent().html());
	if(users.length<pageSize){
		for(var i=users.length;i<pageSize;i++){
			createEmptyElement(i);
		}
	}
}

*//**
 * 权限配置页面添加空白行
 *//*
function createEmptyElement(index){
	var tbodyobj = document.createElement("tbody");
	var rowobj = document.createElement('tr'); //创建一个tr元素
	tbodyobj.appendChild(rowobj);;
	rowobj.setAttribute("name","row");
	var bgcolor = index%2==0 ? "#efefef":"#FFFFFF";
	rowobj.appendChild($(' <td height="24" name="id" bgcolor="'+bgcolor+'"></td>')[0]);
	rowobj.appendChild($(' <td name="name" bgcolor="'+bgcolor+'" ></td>')[0]);
	rowobj.appendChild($('<td bgcolor="'+bgcolor+'"></td>')[0]);
	rowobj.appendChild($('<td align="center" bgcolor="'+bgcolor+'"></td>')[0]);
	rowobj.appendChild($('<td align="center" bgcolor="'+bgcolor+'"></td>')[0]);
	document.getElementById('usertable').appendChild(tbodyobj);//
}

function showUserRole(element){
	var userId =  $(element).find("td:first").html();
	var name = $(element).find("td:first").next().html();
	$('#selectUser').html("用户功能清单："+userId+"@"+name);
	$('#selectUser').attr("userId",userId);
	$.ajax({
		type : "POST",
		dataType : "json",
		async : false,
		url : "getRoleByUserId.action",
		data:{
			id : userId
		},
		success : function(res) {
			if(res.code == 200){
				$("#own").empty();
				$("#unOwn").empty();
				$.each(res.user.roleList,function(index,role) {  
					$("#own").append("<option value='"+role.roleId+"'>"+role.roleName+"</option>");
					res.roles.splice(role,1);
				});
				$.each(res.roles,function(index,role) { 
					$("#unOwn").append("<option value='"+role.roleId+"'>"+role.roleName+"</option>");
				});
			}else{
				window.location ="login.html";
			}
		}
	});
}

//权限配置界面---修改功能
function editUserRole(element){
	var userId =  $(element).find("td:first").html();
	var name = $(element).find("td:first").next().html();
	$('#selectUser').html("用户功能清单："+userId+"@"+name);
	$('#selectUser').attr("userId",userId);//保存的时候要取值
	$.ajax({
		type : "POST",
		dataType : "json",
		async : false,
		url : "getRoleByUserId.action",
		data:{
			id : userId
		},
		success : function(res) {
			if(res.code == 200){
				$("#own").empty();//右边
				$("#unOwn").empty();//左边
				$.each(res.user.roleList,function(index,role) {  
					$("#own").append("<option value='"+role.roleId+"'>"+role.roleName+"</option>");
					//res.roles.splice(role,1); //向/从数组中添加/删除项目，然后返回被删除的项目
					$.each(res.roles,function(index1,item){
						if(item.roleId==role.roleId){
							res.roles=remove(res.roles,index1+1);
						}
					});
				});
				$.each(res.roles,function(index,role) { 
					$("#unOwn").append("<option value='"+role.roleId+"'>"+role.roleName+"</option>");
				});
				$("input").not("[id=save]").each(function(){
					$(this).attr("disabled",false);
				});
				$("input").filter("[id=save]").each(function(){
					$(this).attr("disabled",false);
				});
			}else{
				window.location ="login.html";
			}
		}
	});
}
*//**
 * 删除数组第 index 个位置的数据
 * @param index
 * @returns
 * @author JWF
 *//*
function remove(obj,index) {
    //检查index位置是否有效  
    if(index >= 0 && index <= obj.length){  
        var part1 = obj.slice(0, index);  
        var part2 = obj.slice(index);  
        
        part1.pop(); 
        return (part1.concat(part2));  
    }  
    return obj;  
};
*//**
 * 权限配置界面---保存按钮
 * @author JWF
 *//*
function saveRoles(){
	var role = [];
	var userId=$('#selectUser').attr("userId");
	$("#own option").each(function(){ //filter("select[id=unOwn]")
	    role.push(this.value);
    });
	$.ajax({
		type : "GET",
		dataType : "json",
		async : false,
		url : "saveUserRoles.action",
		data:{
			id : userId,
			role : JSON.stringify(role)
		},
		success : function(res) {
			if(res.code == 200){
				$("input").filter("[id=save]").attr("disabled",true);
				window.location ="manager.html";
			}else{
				window.location ="login.html";
			}
		}
	});
}

*//**
 * 权限配置---添加按钮
 * 限制只能选择一个权限
 * @author JWF
 *//*
function addRoles(){
	if($("#unOwn option:selected").length==0) return;
	if($("#own option").length<=1) {
		$("#own option").each(function(){ //filter("select[id=unOwn]")
			$("#unOwn").append("<option value='"+this.value+"'>"+this.text+"</option>");
			$(this).remove();
		});
		$("#unOwn option:selected").each(function(){ //filter("select[id=unOwn]")
			$("#own").append("<option value='"+this.value+"'>"+this.text+"</option>");
			$(this).remove();
		});
		$("input").filter("[id=save]").attr("disabled",false);//过滤器
	}
}
*//**
 * 角色管理界面---添加按钮
 * @author JWF
 *//*
function addModules(){
	if($("#jsbh").val()=="" || $("#jsmc").val()=="") return;
	if($("#unOwn option:selected").length==0) return;
	$("#unOwn option:selected").each(function(){ //filter("select[id=unOwn]")
		$("#own").append("<option value='"+this.value+"'>"+this.text+"</option>");
		$(this).remove();
	});
	$("input").filter("[id=save]").attr("disabled",false);//过滤器
}
*//**
 * 角色管理界面---全选按钮
 * @author JWF
 *//*
function addAllModules(){
	if($("#jsbh").val()=="" || $("#jsmc").val()=="") return;
	$("#unOwn option").each(function(){ //filter("select[id=unOwn]")
		$("#own").append("<option value='"+this.value+"'>"+this.text+"</option>");
		$(this).remove();
	});
}
*//**
* 角色管理界面---全删按钮
 * @author JWF
 *//*
function removeAllModules(){
	$("#own option").each(function(){ //filter("select[id=unOwn]")
		$("#unOwn").append("<option value='"+this.value+"'>"+this.text+"</option>");
		$(this).remove();
	});
}
*//**
 * 删除按钮
 *//*
function removeRoles(){
	if($("#own option:selected").length==0) return;
	$("#own option:selected").each(function(){ //filter("select[id=unOwn]")
		$("#unOwn").append("<option value='"+this.value+"'>"+this.text+"</option>");
		$(this).remove();
	});
	$("input").filter("[id=save]").attr("disabled",false);
}



*//**
 * 角色管理界面---保存按钮
 * @author JWF
 *//*

function saveModules(){
	var modules = [];
	var roleName=$("#jsmc").val();
	var roleId=$("#jsbh").val();
	if(roleId=="" || roleName=="") {
		alert("角色信息请填写完整！");
		return;
	}else {
		$("#own option").each(function(){ //filter("select[id=unOwn]")
			modules.push(this.value);
	    });
		if($("#jsbh").attr("readonly")=="readonly"){
			saveRolemodules(roleId,modules,roleName);
			return;
		}
		$.ajax({
			type : "GET",
			dataType : "json",
			async : false,
			url : "validateRole.action",
			data : {
				id : roleId
			},
			success : function(res) {
				if(res.code==200) {
					if(res.result==1){
						alert("编号为："+roleId+" 的角色已存在，请更换编号！");
					}else {
						saveRolemodules(roleId,modules,roleName);
					}
				}else{
					window.location ="login.html";
				}
			}
		});
	}
}

*//**
 * 保存数据
 * @author JWF
 * @param roleId
 * @param modules
 * @param roleName
 *//*
function saveRolemodules(roleId,modules,roleName){
	$.ajax({
		type : "POST",
		dataType : "json",
		async : false,
		url : "saveRolemodules.action",
		data:{
			id : roleId,
			modules : JSON.stringify(modules),
			name:roleName
		},
		success : function(res) {
			if(res.code == 200){
				$("input").attr("disabled",true);
				window.location ="manager6.html";
			}else{
				window.location ="login.html";
			}
		}
	});
}
*//**
 * 角色管理的修改Modules
 * @param element
 *//*
function editRole(element){
	var roleId =  $(element).find("td:first").html();
	var name = $(element).find("td:first").next().html();
	$('#jsbh').val(roleId);
	$('#jsmc').val(name);
	//$("input").filter("[id=jsbh]").attr("disabled",false);
	$('#jsbh').attr("readonly","true");
	$("#tips").show();
	$("#tip").html("提示信息：角色编号不可修改！");
	$('#selectRole').attr("roleId",roleId);
	$.ajax({
		type : "POST",
		dataType : "json",
		async : false,
		url : "getModuleByRoleId.action",
		data:{
			id : roleId
		},
		success : function(res) {
			if(res.code == 200){
				$("#own").empty();//右边
				$("#unOwn").empty();//左边
				$.each(res.rolesExit,function(index,role) {  
					$("#own").append("<option value='"+role.roleId+"'>"+role.roleName+"</option>");
					$.each(res.roles,function(index1,item){ //去除已选择的module
						if(item.roleId==role.roleId){
							res.roles=remove(res.roles,index1+1);
						}
					});
					//res.roles.splice(role,1); //向/从数组中添加/删除项目，然后返回被删除的项目
				});
				$.each(res.roles,function(index,role) { 
					$("#unOwn").append("<option value='"+role.roleId+"'>"+role.roleName+"</option>");
				});
				$("input").each(function(){
					$(this).attr("disabled",false);
				});
			}else{
				window.location ="login.html";
			}
		}
	});
}

*//**
 * 删除角色
 * @author JWF
 *//*
function deleteRole(element) {
	var roleId =  $(element).find("td:first").html();
	if (confirm("你确定要删除编号为："+roleId+" 的角色吗？")) {  
		$.ajax({
			type : "POST",
			dataType : "json",
			async : false,
			url : "deleteRole.action",
			data:{
				id : roleId
			},
			success : function(res) {
				if(res.code == 200){
					$("input").filter("[id=save]").attr("disabled",true);
					window.location ="manager6.html";
				}else{
					window.location ="login.html";
				}
			}
		});
	}
}
*//**
 * 角色管理界面初始化方法
 * @author JWF
 * 2016年7月18日
 *//*
function RoleManager() {
	var pageSize = 8;
	var pageCount = 1;
	$.ajax({
		type : "GET",
		dataType : "json",
		async : false,
		url : "getRole.action",
		data : {
			page : 1,
			pageSize : pageSize
		},
		success : function(res) {
			if(res.code == 200){
				$("#personName").html("<span>"+res.user.name+"</span>");
					pageCount = res.total%pageSize==0 ? res.total/pageSize:res.total/pageSize+1;
					pageCount = Math.floor(pageCount);
					loadRoles(res.roleList);
			}else{
				window.location ="login.html";
			}
		}
	});
	$(".pager").createPage({
        pageCount:pageCount,
        current:1,
        backFn:function(p){
        	$.ajax({
        		type : "GET",
        		dataType : "json",
        		async : false,
        		url : "getRole.action",
        		data : {
        			page : p,
        			pageSize : pageSize
        		},
        		success : function(res) {
        			if(res.code == 200){
        				$("table tr").filter("tr[name=row]").each(function(){ //filter("select[id=unOwn]")  
        				    $(this).remove();
        			        });
        				loadRoles(res.roleList);
        			}else{
        				window.location ="login.html";
        			}
        		}
        	});
        }
    });
}

*//**
 * 加载角色列表数据
 * @author JWF
 *//*
function loadRoles(roles){
	$.each(roles,function(index,role) {  
		var tbodyobj = document.createElement("tbody");
		var rowobj = document.createElement('tr'); //创建一个tr元素
		tbodyobj.appendChild(rowobj);
		rowobj.setAttribute("name","row");
		rowobj.style = "cursor: pointer";
		rowobj.id = "row"+role.roleId;
		//$('#selectRole').attr("roleId",role.roleId);
		var bgcolor = index%2==0 ? "#efefef":"#FFFFFF";
		rowobj.appendChild($('<td height="24" align="center" name="roleId" bgcolor="'+bgcolor+'">'+role.roleId+'</td>')[0]);
		rowobj.appendChild($('<td height="24" name="roleName" bgcolor="'+bgcolor+'">'+role.roleName+'</td>')[0]);
		rowobj.appendChild($('<td height="24" align="center" name="roleId" bgcolor="'+bgcolor+'"><a href="javascript:void(0)"onclick="editRole('+rowobj.id+')">修改</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:void(0)" onclick="deleteRole('+rowobj.id+')">删除</a></td>')[0]);
		document.getElementById('roletable').appendChild(tbodyobj);
	});
	if(roles.length<8){
		for(var i=roles.length;i<8;i++){
			createEmptyforRole(i);
		}
	}
}
*//**
 * 角色管理页面添加空白行
 * @author JWF
 *//*
function createEmptyforRole(index){
	var tbodyobj = document.createElement("tbody");
	var rowobj = document.createElement('tr'); //创建一个tr元素
	tbodyobj.appendChild(rowobj);
	rowobj.setAttribute("name","row");
	var bgcolor = index%2==0 ? "#efefef":"#FFFFFF";
	rowobj.appendChild($('<td height="24" name="id" bgcolor="'+bgcolor+'"></td>')[0]);
	rowobj.appendChild($('<td height="24" name="id" bgcolor="'+bgcolor+'" ></td>')[0]);
	rowobj.appendChild($('<td height="24" name="id" bgcolor="'+bgcolor+'"></td>')[0]);
	document.getElementById('roletable').appendChild(tbodyobj);
}
function showKeyPress(evt) {
	alert(evt);
	 evt = (evt) ? evt : window.event;
	 alert(evt.keyCode);
	 return checkSpecificKey(evt.keyCode);
	}

	function checkSpecificKey(keyCode) {
	    var specialKey = "#$%\^*\'\"\+";//Specific Key list
	    var realkey = String.fromCharCode(keyCode);
	    var flg = false;
	 flg = (specialKey.indexOf(realkey) >= 0);
	  if (flg) {
	        alert('请勿输入特殊字符: ' + realkey);
	        return false;
	    }
	    return true;
	}
*//**
 * 新增角色
 * @author JWF
 * 2016年7月19日
 *//*
function addRole(){
	$("#jsbh").val("");
	$("#jsbh").removeAttr("readonly");
	$("#jsmc").val("");
	$("#tips").show();
	$("#tip").html("提示信息：角色编号只允许输入英文字母、数字或者下划线；角色名称只允许输入汉字。");
	$("#own").empty();//右边
	$("#unOwn").empty();//左边
	$("input").each(function(){
		$(this).attr("disabled",false);
	});
	getModules();
}

function insertRoleXML(roleId,jsmc){
	$.ajax({
		type : "GET",
		dataType : "json",
		async : false,
		url : "insertRoleXML.action",
		data : {
			id : roleId,
			roleName: jsmc
		},
		success : function(res) {
			if(res.code==200) {
				window.location ="manager6.html";
			}
		}
	});
}

*//**
 * 获取所有的Modules
 *//*
function getModules(){
	$.ajax({
		type : "POST",
		dataType : "json",
		async : false,
		url : "getModuleList.action",
		success : function(res) {
			if(res.code==200) {
				$.each(res.modules,function(index,module) { 
					$("#unOwn").append("<option value='"+module.roleId+"'>"+module.roleName+"</option>");
				});
				$("input").not("[id=save]").each(function(){
					$(this).attr("disabled",false);
				});
			}
		}
	});
}
*//**
 * 功能配置界面初始化
 *//*
function initRoleManager(){
	var pageSize = 16;
	var pageCount = 1;
	$.ajax({
		type : "GET",
		dataType : "json",
		async : false,
		data:{
			page:1,
			pageSize:pageSize
		},
		url : "getModules.action",
		success : function(res) {
			if(res.code == 200){
				$.each(res.roleList,function(index,role) {          					
					var tbodyobj = document.createElement("tbody");
					var rowobj = document.createElement('tr'); //创建一个tr元素
					tbodyobj.appendChild(rowobj);
					rowobj.setAttribute("name","row");
					rowobj.style = "cursor: pointer";
					var bgcolor = index%2==0 ? "#efefef":"#FFFFFF";
					rowobj.appendChild($('<td height="24" align="center" name="roleId" bgcolor="'+bgcolor+'">'+role.roleId+'</td>')[0]);
					rowobj.appendChild($('<td ondblclick="ShowElement(this)" name="roleName" bgcolor="'+bgcolor+'">'+role.roleName+'</td>')[0]);
					rowobj.appendChild($('<td ondblclick="ShowElement(this)" name="URL" bgcolor="'+bgcolor+'">'+role.URL+'</td>')[0]);
					rowobj.appendChild($('<td align="center" bgcolor="'+bgcolor+'">'+user.gender+'</td>')[0]);
					rowobj.appendChild($('<td align="center" bgcolor="'+bgcolor+'"><a href="javascript:void(0)" onclick="editUserRole('+rowobj.id+')">修改</a></td>')[0]);
					document.getElementById('roletable').appendChild(tbodyobj);
				});
				pageCount = res.total%pageSize==0 ? res.total/pageSize:res.total/pageSize+1;
				pageCount = Math.floor(pageCount);
				if(res.roleList.length<pageSize){
					for(var i=res.roleList.length;i<pageSize;i++){
						createEmptyforRole(i);
					}
				}
			}else{
				window.location ="login.html";
			}
		}
	});
	
	$(".pager").createPage({
        pageCount:pageCount,
        current:1,
        backFn:function(p){
        	$.ajax({
        		type : "GET",
        		dataType : "json",
        		async : false,
        		data : {
        			page : p,
        			pageSize : pageSize
        		},
        		url : "getModules.action",
        		success : function(res) {
        			if(res.code == 200){
        				$("table tr").filter("tr[name=row]").each(function(){ //filter("select[id=unOwn]")  
        				    $(this).remove();
        			        });
        				$.each(res.roleList,function(index,role) {          					
        					var tbodyobj = document.createElement("tbody");
        					var rowobj = document.createElement('tr'); //创建一个tr元素
        					tbodyobj.appendChild(rowobj);
        					rowobj.setAttribute("name","row");
        					rowobj.style = "cursor: pointer";
        					var bgcolor = index%2==0 ? "#efefef":"#FFFFFF";
        					rowobj.appendChild($('<td height="24" align="center" name="roleId" bgcolor="'+bgcolor+'">'+role.roleId+'</td>')[0]);
        					rowobj.appendChild($('<td ondblclick="ShowElement(this)" name="roleName" bgcolor="'+bgcolor+'">'+role.roleName+'</td>')[0]);
        					rowobj.appendChild($('<td ondblclick="ShowElement(this)" name="URL" bgcolor="'+bgcolor+'">'+role.URL+'</td>')[0]);
        					rowobj.appendChild($('<td align="center" bgcolor="'+bgcolor+'">'+user.gender+'</td>')[0]);
        					rowobj.appendChild($('<td align="center" bgcolor="'+bgcolor+'"><a href="javascript:void(0)" onclick="editUserRole('+rowobj.id+')">修改</a></td>')[0]);
        					document.getElementById('roletable').appendChild(tbodyobj);
        				}); 
        				if(res.roleList.length<pageSize){
        					for(var i=res.roleList.length;i<pageSize;i++){
        						createEmptyforRole(i);
        					}
        				}
        			}else{
        				window.location ="login.html";
        			}
        		}
        	});
        }
    });
}

function initIndexRole(){
	$.ajax({
		type : "POST",
		dataType : "json",
		async : false,
		url : "getAllRoles.action",
		success : function(res) {
			if(res.code == 200){
				$.each(res.roleList,function(index,role) {  
					if(role.URL){
						$("div").filter("[name=div"+role.roleId+"]").each(function(){
							$(this).find("a").each(function(){
								$(this).attr('href',role.URL);
								$(this).attr('target',"_blank");
							});
						});
					}
					
				});  
			}else{
				window.location ="login.html";
			}
			
		}
	});
}


function initIndex(){
	$.ajax({
		type : "GET",
		dataType : "json",
		async : false,
		url : "getUser.action",
		success : function(res) {
			if(res.code == 200){
				if(res.user.emptyModule){
					for(var module in res.user.emptyModule){
						var id = res.user.emptyModule[module];
						$("a").filter('[data-modal='+id+']').parent().removeAttr("onmouseover");
//						$("a").filter('[data-modal='+id+']').parent().attr("onmouseover","moveTip(this)");
						var div = $("a").filter('[data-modal='+id+']').parent();
						$(div).hover(function() {
					        var p = $(this).position();
					    	$("em").css("top",p.top + $(this).height()+50);
					    	$("em").css("left",p.left+($(this).width()-$("em").width())/2);
					    	$("em").show();	
						}, function() {
							$("em").css("display", "none");  
						});
						var html = $("a").filter('[data-modal='+id+']').html();
						$("a").filter('[data-modal='+id+']').parent().append(html);
						$("a").filter('[data-modal='+id+']').remove();
					}
				}
				if(res.user.moduleMap){
					for(var module in res.user.moduleMap){
						var divobj = document.createElement('div'); //创建一个tr元素
						divobj.setAttribute("class","TCK_ML"); 
						if(res.user.moduleMap[module].length>6){	
							var newClass_left = module+"_left";
							var newClass_right = module+"_right";
							$(document.getElementById(module)).append('<div class="og_prev '+newClass_left+'"></div>'
									+'<div class="og_next '+newClass_right+'"></div>');	
					       divobj.id="TCK_ML_"+module;
							$("."+newClass_left).click(function(){
								var id = "TCK_ML_"+$(this).parent().attr("id");
								$("#"+id).css({marginLeft:"-230px"}).find("ul:last").prependTo($("#"+id));
								$("#"+id).animate({marginLeft:"0px"},300);
					       });
					       $("."+newClass_right).click(function(){
					    	   var id = "TCK_ML_"+$(this).parent().attr("id");
					    	   $("#"+id).animate({marginLeft:"-230px"},300,function(){
					            $(this).css({marginLeft:0}).find("ul:first").appendTo($("#"+id));
					            });    
					       });  
						}
						var ulobj = null;
						$.each(res.user.moduleMap[module],function(index,role) {
							if(index % 6 == 0){
								ulobj = document.createElement('ul');
								divobj.appendChild(ulobj);
							}
							var liobj = document.createElement('li');
							var name = role.roleName.length<=8?role.roleName+"<br/><br/>":role.roleName;
							liobj.innerHTML='<div><a href="'+role.URL+'" target="_blank"><img src="'+role.img+'"/></a></div>'+
								'<div><a href="'+role.URL+'" target="_blank">'+name+'</a></div>';
							ulobj.appendChild(liobj);
						});
						var index = res.user.moduleMap[module].length;
						//生成空li
						while(index%6 < 4){
							var liobj = document.createElement('li');
							ulobj.appendChild(liobj);
							index = index +1;
						}
						//$(divobj).append('<dl style=" text-align:center; padding-top:10px; display:none;"><span style="padding-right:50px;"><a href="#"><img src="resources/images/pre.png" width="33" height="35" /></a></span><span><a href="#"><img src="resources/images/next.png" width="33" height="35" /></a></span></dl>');
						document.getElementById(module).appendChild(divobj);
					}
				}
			}else{
				window.location ="login.html";
			}
			
		}
	});
}

function moveTip(element){
	var div = $("#receiptInfo"); //要浮动在这个元素旁边的层  
    div.css("position", "absolute");//让这个层可以绝对定位 
    var self = $(element); //当前对象  
    self.hover(function() {  
        div.css("display", "block");  
        var p = self.position(); //获取这个元素的left和top  
        var x = p.left + self.width();//获取这个浮动层的left  
//        alert(self.height());
        var docWidth = $(document).width();//获取网页的宽  
        if (x > docWidth - div.width() - 20) {  
            x = p.left - div.width();  
        }  
//        alert(p.top);
        div.css("left", p.left+self.width()/2-div.width()/2);  
        div.css("top", p.top+self.height()/2-div.height()/2);  
        div.show();  
    },  
    function() {  
        div.css("display", "none");  
    } 
    );
}

function moveTip(element){
    $(element).hover(function() {
        var p = $(element).position();
    	$("em").css("top",p.top + $(element).height()+50);
    	$("em").css("left",p.left+($(element).width()-$("em").width())/2);
    	$("em").show();	
	}, function() {
		$("em").css("display", "none");  
//		$("em").animate({opacity: "hide", top: "200"}, "fast");
	});
}*/