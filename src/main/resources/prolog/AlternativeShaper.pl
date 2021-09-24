/*
============================
============================
   Alternative Shaper
============================
============================
*/

	:- op(140, fy, @).

	:- op(160, xfy, ?).

	:- pce_global(@working_shape, new(picture('Alternative Shaper - Working Shape'))).
	:- pce_global(@selected_shape, new(text_item(selected_shape,'no selected shape'))).
	:- pce_global(@selected_procedure, new(text_item(selected_procedure, 'no selected procedure'))).
	:- pce_global(@selected_memory, new(text_item(selected_memory, 'global'))).
	:- pce_global(@feedback, new(text(''))).

	:- pce_global(@image_viewer_icon_spatial,
	new(spatial(xref=x+w/2, yref=y+h+5,
			  xref=x+w/2, yref=y))).

	:- pce_global(@image_viewer_icon_spatial2,
	new(spatial(xref=x+w+10, yref=y,
			  xref=x, yref=y))).

:- use_module(library('plot/axis')).

:- multifile shapeComposition/2.
:- multifile memory/2.
:- multifile ruleProcedure/2.
:- discontiguous shapeRule/2.
:- discontiguous applyRuleProcedure/5.
:- dynamic picture/1, shape/2, shapeComposition/2, basicShapeDimention/3, shapeDimention/4, memory/2, message/2.
	:- load_files('Design.txt',[all_dynamic(true)]).
	:- load_files('DesignSpecific.pl',[all_dynamic(true)]).
	:- set_prolog_flag(answer_write_options,[max_depth(0)]).

	% parametic shapes

	shape(x, 'shapes/x.gif').
shape(y, 'shapes/y.gif').
shape(z, 'shapes/z.gif').
shape(w, 'shapes/w.gif').
shape(i, 'shapes/i.gif').
	shape(j, 'shapes/j.gif').
	shape(k, 'shapes/k.gif').
	shape(l, 'shapes/l.gif').
	shape(m, 'shapes/m.gif').
shape(n, 'shapes/n.gif').
shape(susp, 'shapes/susp.gif').
	basicShapeDimention(x,0.6,0.6).
basicShapeDimention(y,0.6,0.6).
basicShapeDimention(z,0.6,0.6).
basicShapeDimention(w,0.6,0.6).
	basicShapeDimention(i,0.6,0.6).
	basicShapeDimention(j,0.6,0.6).
	basicShapeDimention(k,0.6,0.6).
	basicShapeDimention(l,0.6,0.6).
	basicShapeDimention(m,0.6,0.6).
basicShapeDimention(n,0.6,0.6).
	basicShapeDimention(susp,0.6,0.6).

	/*
============================
============================
Alternative Shaper Interface
============================
============================
*/

as :-	shape_composition_viewer,
	shape_rule_viewer,
	rule_procedure_viewer,
	shape_generation_viewer,
	input_viewer,
	assert(design_number(0)).

		/*
============================
   Shape Generation Viewer
============================
*/

shape_generation_viewer :-
	assert(picture(@working_shape)),
	send(@working_shape, normalise, area(-410,-410, 410,410), xy),
	send(@working_shape, size, size(820,820)),
	send(@working_shape, display, plot_axis(x, -10, 10, 1, 800, point(-400, 0))),
send(@working_shape, display, plot_axis(y, -10, 10, 1, 800, point(0, 400))),
send(new(D, dialog), below, @working_shape),
	send(D, append, @selected_shape),
	new(ValueSet1, chain('no selected shape')),
	findall(SShapeId, (shapeComposition(ShapeId,_), term_string(ShapeId,SShapeId)), L1),
	send_list(ValueSet1, append, L1),
	send(@selected_shape, value_set, ValueSet1),
	send(D, append, @selected_procedure),
	new(ValueSet2, chain('no selected procedure')),
	findall(SProcedureId, (ruleProcedure(ProcedureId,_), term_string(ProcedureId,SProcedureId)), L2),
	send_list(ValueSet2, append, L2),
	send(@selected_procedure, value_set, ValueSet2),
	send(D, append, @selected_memory),
	new(ValueSet3, chain('no selected memory')),
	findall(SMemoryId, (memory(MemoryId,_), term_string(MemoryId,SMemoryId)), L3),
	send_list(ValueSet3, append, L3),
	send(@selected_memory, value_set, ValueSet3),
	send(D, append, button(all, message(@prolog, thread_send_message, ask_more_alternatives, all))),
	send(D, append, button(generate, message(@prolog, thread_send_message, ask_more_alternatives, generate))),
	send(D, append, button(another,message(@prolog, thread_send_message, ask_more_alternatives, another))),
	send(D, append, button(hold,message(@prolog, thread_send_message, ask_more_alternatives, hold))),
	send(D, append, button(save,message(@prolog, save_design))),
	send(D, append, button(quit, message(@prolog, destroy))),
	send(new(E, dialog), below, D),
	send(E,append(@feedback)),
	message_queue_create(ask_more_alternatives),
	thread_create(generate(@working_shape, @selected_shape, @selected_procedure, @selected_memory), _, []),
	send(@working_shape, open).

		generate(P, T1, T2, T3) :-
	send(@feedback, clear), send(@feedback, insert, 0, 'Select options and Choose an action: Generate, Save, Quit'), send(@feedback, flush),
	thread_get_message(ask_more_alternatives,Y),
	((Y == generate,
		get(T1, displayed_value, SId), get(SId, value, ShapeId),
		get(T2, displayed_value, PId), get(PId, value, ProcedureId),
		get(T3, displayed_value, MId), get(MId, value, MemoryId),
		shapeComposition(ShapeId,InitialShapeComposition),
		memory(MemoryId, InitialMemory),
		send(@feedback, clear), send(@feedback, insert, 0, 'Processing Generate ...'), send(@feedback, flush),
		applyRuleProcedure(ProcedureId, InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory),
		send(P?graphicals, for_all, if(message(@arg1, instance_of, bitmap), message(@arg1, free))),
shapesbringtofront(L),
	filterby(FinalShapeComposition,L,FilteredShapeComposition,OppositeFilteredShapeComposition),
	merge(OppositeFilteredShapeComposition,FilteredShapeComposition,FilteredFinalShapeComposition),
	displayShapebyShape(FilteredFinalShapeComposition, P),
	send(P, flush),
	send(@feedback, clear), send(@feedback, insert, 0, 'Select options and Choose an action: Generate, Another, Hold, Save, Quit'), send(@feedback, flush),
	thread_get_message(ask_more_alternatives,X),
	((X == another, send(@feedback, clear), send(@feedback, insert, 0, 'Processing Another ...'), send(@feedback, flush), fail);
(X == hold,
	delete_saved_shape,
	assert(shapeComposition(savedShape, FinalShapeComposition)),
	get(T1, value_set, ValueSet1),
	send(ValueSet1,delete_all, savedShape),
	send(ValueSet1, prepend, savedShape),
	send(T1, value_set, ValueSet1),
	send(T1, selection, savedShape), send(T1, flush),
	delete_saved_memory,
	assert(memory(savedMemory, FinalMemory)),
	get(T3, value_set, ValueSet2),
	send(ValueSet2,delete_all, savedMemory),
	send(ValueSet2, prepend, savedMemory),
	send(T3, value_set, ValueSet2),
	send(T3, selection, savedMemory), send(T3, flush), !,
	generate(P, T1, T2, T3));
(X == generate, thread_send_message(ask_more_alternatives,generate), !, generate(P, T1, T2, T3)))
	);
(Y == all,
	get(T1, displayed_value, SId), get(SId, value, ShapeId),
	get(T2, displayed_value, PId), get(PId, value, ProcedureId),
	get(T3, displayed_value, MId), get(MId, value, MemoryId),
	shapeComposition(ShapeId,InitialShapeComposition),
	memory(MemoryId, InitialMemory),
	send(@feedback, clear), send(@feedback, insert, 0, 'Processing All ...'), send(@feedback, flush),
	applyRuleProcedure(ProcedureId, InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory),
	incdesign_number(N),
	string_concat('savedShape', N, SS), atom_string(ASS,SS),
	assert(shapeComposition(ASS, FinalShapeComposition)),
	get(T1, value_set, ValueSet1),
	send(ValueSet1,delete_all, ASS),
	send(ValueSet1, prepend, ASS),
	send(T1, value_set, ValueSet1),
	send(T1, selection, ASS),
	string_concat('savedMemory', N, MS), atom_string(AMS,MS),
	assert(memory(AMS, FinalMemory)),
	get(T3, value_set, ValueSet2),
	send(ValueSet2,delete_all, AMS),
	send(ValueSet2, prepend, AMS),
	send(T3, value_set, ValueSet2),
	send(T3, selection, AMS),
	send(P?graphicals, for_all, if(message(@arg1, instance_of, bitmap), message(@arg1, free))),
shapesbringtofront(L),
	filterby(FinalShapeComposition,L,FilteredShapeComposition,OppositeFilteredShapeComposition),
	merge(OppositeFilteredShapeComposition,FilteredShapeComposition,FilteredFinalShapeComposition),
	displayShapebyShape(FilteredFinalShapeComposition, P), send(T1, flush), send(T3, flush), send(P, flush), sleep(1), fail
	)).
generate(P, T1, T2, T3) :- generate(P, T1, T2, T3).

	incdesign_number(N) :- retract(design_number(M)), !, N is M+1, assert(design_number(N)).

	destroy :- destroy_all_picture, delete_saved_shape, delete_saved_memory, message_queue_destroy(ask_more_alternatives).

	destroy_all_picture :-
	picture(P),
	free(P),
	retract(picture(P)),
	fail.
		destroy_all_picture.

		delete_saved_shape :- retract(shapeComposition(savedShape, _)), !.
	delete_saved_shape.

	delete_saved_memory :- retract(memory(savedMemory, _)), !.
	delete_saved_memory.

	% save_design

save_design :-
	tell('DesignSaved.txt'),
	save_control_information, nl,
	save_basic_shapes_dimention, nl,
	save_basic_shapes, nl,
	save_memory, nl,
	save_shape_compositions, nl,
	save_shape_rules, nl,
	save_shape_procedures,
	told.

		save_control_information :-
	scale_unit(U),
	write(scale_unit(U)), write('.'), nl,
	shapesbringtofront(L),
	write(shapesbringtofront(L)), write('.'), nl.

	save_basic_shapes_dimention :-
	basicShapeDimention(ShapeId,H,W),
	write(basicShapeDimention(ShapeId,H,W)), write('.'), nl,
	fail.
		save_basic_shapes_dimention.

		save_basic_shapes :-
	shape(ShapeId, ShapeFile),
	write(shape(ShapeId,ShapeFile)), write('.'), nl,
	fail.
		save_basic_shapes.

		save_memory :-
	memory(MemoryId,Memory),
	write(memory(MemoryId,Memory)), write('.'), nl,
	fail.
		save_memory.

		save_shape_compositions :-
	shapeComposition(ShapeCompositionid, ShapeComposition), not(shape(ShapeCompositionid, _)),
	write(shapeComposition(ShapeCompositionid, ShapeComposition)), write('.'), nl,
	fail.
		save_shape_compositions.

		save_shape_rules :-
	shapeRule(ShapeRuleId, ShapeRule),
	write(shapeRule(ShapeRuleId, ShapeRule)), write('.'), nl,
	fail.
		save_shape_rules.

		save_shape_procedures :-
	ruleProcedure(RuleProcedureId, RuleProcedure),
	write(ruleProcedure(RuleProcedureId, RuleProcedure)), write('.'), nl,
	fail.
		save_shape_procedures.

		% displayShapebyShape(ShapeComposition, Picture)

displayShapebyShape([], _).
	displayShapebyShape([s(N,T) | LS], P) :-
	shape(N,File), !,
	new(I,image(File)),
	shapeBasicTransformations(s(N,T), scale(XS,YS), rotation(A), translation(XT,YT)),
	new(M, bitmap(I)),
	get(M, height, HO),
	get(M, width, WO),
	SXS is WO*XS,
	SYS is HO*YS,
	% get(I, scale, size(SXS,SYS), J), scale with problems in swi-prolog
	% get(J, rotate, A, K),
	get(I, rotate, A, K),
	new(L, bitmap(K)),
	get(L, height, HN),
	get(L, width, WN),
	scale_unit(SU),
	((A =< 90, X is XT*40-SYS*sin(A*pi/180), Y is -YT*SU-HN);
	 (A > 90, A =< 180, X is XT*SU-WN, Y is -YT*SU-SXS*sin(A*pi/180));
	 (A > 180, A =< 270, X is XT*40+SXS*cos(A*pi/180), Y is -YT*40);
	 (A > 270, X is XT*SU, Y is -YT*SU+SXS*sin((A+90)*pi/180)-HN)),
	send(P, display, L, point(X, Y)),
	displayShapebyShape(LS, P).
displayShapebyShape([s(N,T)| LS], P) :-
	shapeComposition(N, SC), !,
	applyTransformation(T, SC, SCT),
	displayShapebyShape(SCT, P),
	displayShapebyShape(LS, P).
		displayShapebyShape([N| LS], P) :-
	shapeComposition(N, SC), !,
	displayShapebyShape(SC, P),
	displayShapebyShape(LS, P).
		displayShapebyShape(N, P) :-
	shapeComposition(N, SC), !,
	displayShapebyShape(SC, P).

		% filterby(ShapeComposition,LFilter,FilteredShapeComposition,OppositeFilteredShapeComposition)

filterby([s(N,T)|L],LF,[s(N,T)|Filtered],Opposite) :-
	member(N,LF), !,
	filterby(L,LF,Filtered,Opposite).
		filterby([s(N,T)|L],LF,Filtered,[s(N,T)|Opposite]) :-
	filterby(L,LF,Filtered,Opposite),!.
	filterby([N|L],LF,[N|Filtered],Opposite) :-
	member(N,LF), !,
	filterby(L,LF,Filtered,Opposite).
		filterby([N|L],LF,Filtered,[N|Opposite]) :-
	filterby(L,LF,Filtered,Opposite),!.
	filterby([],_,[],[]).

	/*
============================
   Shape Composition Viewer
============================
*/

shape_composition_viewer :-
	new(P, picture),
	assert(picture(P)),
	send(P?frame, label, 'Shape Compositions'),
send(P, scrollbars, vertical),
	send(P, format, format(horizontal, 310, @off)),
	send(P, size, size(310,800)),
	send(new(D, dialog), below, P),
	send(D, append, new(T, text_item(selected_shape, 'no selected shape'))),
	send(P, open),
	make_shape_recogniser(R,T),
	displayAllshapecompositions(P,R).

displayAllshapecompositions(P,R) :-
	shapeComposition(N,ShapeComposition),
	N   =.. [N],
	new(D0, device),
	memory(global,M),
	flattenShapeComposition(ShapeComposition,FShapeComposition,M),
	displayShapebyShape(FShapeComposition, D0),
	send(D0, name, N),
	send(D0, recogniser,R),
	new(F, figure),
	send(F, border, 3),
	send(F, display, D0),
	new(D2, device),
	send(D2, display, F),
	send(D2, display, new(T, text(N, center))),
	send(@image_viewer_icon_spatial, forwards, F, T),
	send(P, display, D2),
	fail.
displayAllshapecompositions(P,R) :-
	shapeComposition(N,ShapeComposition),
	N  =.. [_|Params],
	Params \= [],
	new(D0, device),
	convertparameticShape(Params, ShapeComposition, ConvShapeComposition),
	displayShapebyShape(ConvShapeComposition, D0),
	term_to_atom(N,AN),
	send(D0, name, AN),
	send(D0, recogniser,R),
	new(F, figure),
	send(F, border, 3),
	send(F, display, D0),
	new(D2, device),
	send(D2, display, F),
	send(D2, display, new(T, text(AN, center))),
	send(@image_viewer_icon_spatial, forwards, F, T),
	send(P, display, D2),
	fail.
displayAllshapecompositions(_,_).

% convertparameticShape(Param, ShapeComposition, ConvShapeComposition)

convertparameticShape(_,[],[]):-!.
convertparameticShape(P,[S | LS], CSLS):- !,
	convertparameticShape(P, S, CS),
	convertparameticShape(P,LS,CLS),
	append(CS,CLS,CSLS).
convertparameticShape(_, s(N,T), [s(N,T)]):-
	shape(N,_), !.
convertparameticShape(P, s(N,T), CS):-
	N =.. [N],
	shapeComposition(N,SC), !,
	convertparameticShape(P, SC,CSC),
	applyTransformation(T, CSC, CS).
convertparameticShape(P,s(Shapecall,T), CS):-
	Shapecall  =.. [Shapeid|Args],
	Args \= [], !,
	shapeComposition(Shapedef,LS),
	Shapedef =.. [Shapeid|Params],
	substituteAllParams(LS, Params, Args, SubsLS),
	convertparameticShape(P,SubsLS,CSC),
	applyTransformation(T, CSC, CS).

convertparameticShape(P, pot(S,J,T), CpotJS):-
	member(J,P), !,
	convertparameticShape(P,S,CS),
	matrixpot(T, 1, T1),
	matrixpot(T, 2, T2),
	matrixpot(T, 3, T3),
	matrixpot(T, 4, T4),
	applyTransformation(T4, CS, TCS),
	append(CS, TCS, CSTSCS),
	append(CSTSCS, [s(susp,T1),s(J,T2),s(susp,T3)], CpotJS).

convertparameticShape(P, pot(S,1,_), CS):- !,
	convertparameticShape(P, S, CS).
convertparameticShape(P, pot(S,I,T), CpotIS):- !,
	convertparameticShape(P, S, CS),
	J is I-1, !,
	convertparameticShape(P, pot(S,J,T), CpotJS),
	applyTransformation(T, CpotJS, CpotJST),
	append(CS, CpotJST, CpotIS).
convertparameticShape(P, Shapecall, CSC):-
	Shapecall  =.. [Shapeid|Args],
	Args \= [], !,
	shapeComposition(Shapedef,LS),
	Shapedef =.. [Shapeid|Params],
	substituteAllParams(P, LS, Params, Args, SubsLS),
	convertparameticShape(SubsLS, CSC).
convertparameticShape(P, N, CSC):-
	shapeComposition(N,SC), !,
	convertparameticShape(P, SC, CSC).

make_shape_recogniser(R,T) :-
	new(R, handler_group),
	send(R, append, click_gesture(left, '', single,
				message(@prolog, put_text_item, T, @receiver?name),
				message(@receiver, inverted, @receiver?inverted?negate))).

put_text_item(T, S) :- send(T, displayed_value, S).


/*
============================
   Shape Rule Viewer
============================
*/

shape_rule_viewer :-
	new(P, picture),
	assert(picture(P)),
	send(P?frame, label, 'Shape Rules'),
	send(P, scrollbars, vertical),
	send(P, format, format(horizontal, 310, @off)),
	send(P, size, size(310,800)),
	send(new(D, dialog), below, P),
	send(D, append, new(T, text_item(selected_shape_rule, 'no selected shape rule'))),
	send(P, open),
	make_rule_recogniser(R,T),
	displayAllrules(P,R).

displayAllrules(P,R) :-
	(shapeRule(N,sr(Left,Right));shapeRule(N,sr(Left,Right,Procedure));shapeRule(N,src(Condition,Left,Right));shapeRule(N,src(Condition,Left,Right,Procedure))),
	memory(global,M),
	flattenShapeComposition(Left,FLeft,M),
	flattenShapeComposition(Right,FRight,M),
	shapeDimention(FLeft,HL,WL,M),
	shapeDimention(FRight,_,WR,M),
	DXR is HL + 1.25,
	applyTransformation([1,0,0,0,1,0,DXR,0,1], FRight, TRight),
	new(D0, device),
	displayShapebyShape(FLeft, D0),
	new(I,image('shapes/arrow.gif')),
	new(A, bitmap(I)),
	scale_unit(SU), DXA is HL*SU,
	((WL =< WR, DYA is WR-34);(WL > WR, DYA is WL-34)),
	send(D0, display, A, point(DXA,DYA)),
	displayShapebyShape(TRight, D0),
	term_to_atom(N,AN),
	send(D0, name, AN),
	send(D0, recogniser,R),
	(	(nonvar(Procedure),
		 term_to_atom(Procedure,AP),
		 send(D0, display, new(T0, text(AP, right))),
		 send(D0, display, T0)
		); var(Procedure)),
	(	(nonvar(Condition),
		 term_to_atom(Condition,AC),
		 send(D0, display, new(T1, text(AC, right))),
		 send(D0, display, T1, point(0,14))
		); var(Condition)),
	new(F, figure),
	send(F, border, 10),
	send(F, display, D0),
	new(D2, device),
	send(D2, display, F),
	send(D2, display, new(T, text(AN, center))),
	send(@image_viewer_icon_spatial, forwards, F, T),
	send(P, display, D2),
	fail.

displayAllrules(_,_).

make_rule_recogniser(R,T) :-
	new(R, handler_group),
	send(R, append, click_gesture(left, '', single,
				message(@prolog, put_text_item, T, @receiver?name),
				message(@receiver, inverted, @receiver?inverted?negate))).

/*
============================
   Rule Procedure Viewer
============================
*/

rule_procedure_viewer :-
	new(P, picture),
	assert(picture(P)),
	send(P?frame, label, 'Shape Procedures'),
	send(P, scrollbars, vertical),
	send(P, format, format(horizontal, 310, @off)),
	send(P, size, size(310,800)),
	send(new(D, dialog), below, P),
	send(D, append, new(T, text_item(selected_procedure, 'no selected procedure'))),
	send(P, open),
	make_procedure_recogniser(R,T),
	displayAllprocedures(P,R).

displayAllprocedures(P,R) :-
	ruleProcedure(N,Procedure),
	new(D1, device),
	send(D1, size, size(500,500)),
	term_to_atom(Procedure,AP),
	send(D1, display, new(_, text(AP, center))),
	new(D2, device),
	term_to_atom(N,AN),
	send(D2, name, AN),
	send(D2, recogniser,R),
	send(D2, display, D1),
	send(D2, display, new(T2, text(AN, center))),
	send(@image_viewer_icon_spatial2, forwards, T2, D1),
	send(P, display, D2),
	fail.
displayAllprocedures(_,_).


make_procedure_recogniser(R,T) :-
	new(R, handler_group),
	send(R, append, click_gesture(left, '', single,
				message(@prolog, put_text_item, T, @receiver?name),
				message(@receiver, inverted, @receiver?inverted?negate))).

/*
============================
   Input Viewer
============================
*/

input_viewer :-
	new(Input,dialog('Input Window')),
	assert(picture(Input)),
	send(Input,append(new(J,text('')))),
	send(Input,append(new(I,text_item(answer,'')))),
	message_queue_create(ask_input),
	send(Input,append(button(enter,message(@prolog, return_message, I, J, I?selection)))),
	thread_create(input_interation(Input,I,J), _, []),
	send(Input, open),
	send(I, displayed, @off).

return_message(I, J, S) :-
	S \== '', !,
	send(I, displayed, @off),
	send(J, displayed, @off),
	term_string(A,S),
	thread_send_message(ask_input,answer(A)).
return_message(_, _, _).

input_interation(D,I,J) :-
	thread_get_message(ask_input,question(Q)),
	send(J, clear), send(J, insert, 0, Q),
	send(J, displayed, @on),
	send(I, clear), send(I, displayed, @on),
	send(D,flush), !,
	input_interation(D,I,J).

/*
============================
============================
   Memory.
============================
============================
*/

listvar([],[]).
listvar([var(X,_)|LM],[X|LV]) :- listvar(LM,LV).

listvalue([],[]).
listvalue([var(_,V)|LM],[V|LV]) :- listvalue(LM,LV).

/*
============================
============================
   Shapes and Procedures.
============================
============================
*/

% shape terms: ShapeId, s(Name,[M11, M21, M31, M12, M22, M32, M13, M23, M33]), pot(Shape,I,T)
% shape compositions are lists of shape terms
% shape definitions: shape(ShapeId, Shapefile), shapeComposition(ShapeId,ShapeComposition)
% shape rules terms: sr(ShapeCompositionLeft,ShapeCompositionRight), src(Condition,ShapeCompositionLeft,ShapeCompositionRight), src(Condition,ShapeCompositionLeft,ShapeCompositionRight,RuleProcedure)
% shape rule definitions: shapeRule(RuleId, ShapeRule)
% rule process terms:	ProcedureId, ShapeRuleId, shape rule terms plus sr(L,R,T), srglobal(L,R)
%			assign(Var, Value), input(Msg,Var)
%			or(RuleProcedure, RuleProcedure), seq(RuleProcedure, RuleProcedure), and(RuleProcedure, RuleProcedure), initialShape(ShapeComposition), verify(Condition)
%			pickfirst(RuleProcedure), pickone(RuleProcedure), on(ShapeComposition,RuleProcedure)
% rule procedure definitions: ruleProcedure(RuleProcedureId,RuleProcedure)

shape(s(N,[1,0,0,0,1,0,0,0,1])) :- shape(N,_).
shapeComposition(N, [s(N,[1,0,0,0,1,0,0,0,1])]) :- shape(N,_).
ruleProcedure(N, ShapeRule) :- shapeRule(N, ShapeRule).

% flattenShapeComposition(ShapeComposition,FlattenShapeComposition, Memory)

flattenShapeComposition([],[], _):-!.
flattenShapeComposition([S | LS], RFS, Memory):- !,
	flattenShapeComposition(S, FS, Memory),
	flattenShapeComposition(LS,FLS, Memory),
	append(FS,FLS,RFS).
flattenShapeComposition(s(N,T), [s(N,T)], _):-
	shape(N,_), !.
flattenShapeComposition(s(N,T), FS, Memory):-
	N =.. [N],
	member(var(N,SC),Memory), !,
	flattenShapeComposition(SC,FSC, Memory),
	applyTransformation(T, FSC, FS).
flattenShapeComposition(s(N,T), FS, Memory):-
	N =.. [N],
	shapeComposition(N,SC), !,
	flattenShapeComposition(SC,FSC, Memory),
	applyTransformation(T, FSC, FS).
flattenShapeComposition(s(Shapecall,T), FS, Memory):-
	Shapecall  =.. [Shapeid|Args],
	Args \= [],
	shapeComposition(Shapedef,LS),
	Shapedef =.. [Shapeid|Params],!,
	substituteAllParams(LS, Params, Args, SubsLS, Memory),
	flattenShapeComposition(SubsLS,FSC, Memory),
	applyTransformation(T, FSC, FS).
flattenShapeComposition(s(Shape,T1), FS, Memory):-
	Shape  =.. [s,N,T2], !,
	flattenShapeComposition(N,FSC,Memory),
	applyTransformation(T2, FSC, FSC2),
	applyTransformation(T1, FSC2, FS).
flattenShapeComposition(s(Shape,T), FS, Memory):-
	Shape = [S|LS],!,
	flattenShapeComposition([S|LS],FSC, Memory),
	applyTransformation(T, FSC, FS).
flattenShapeComposition(pot(S,1,_), FS, Memory):- !,
	flattenShapeComposition(S,FS, Memory).
flattenShapeComposition(pot(S,I,T), FpotIS, Memory):- !,
	flattenShapeComposition(S,FS, Memory),
	J is I-1,
	flattenShapeComposition(pot(S,J,T),FpotJS, Memory),
	applyTransformation(T, FpotJS, FpotJST),
	append(FS,FpotJST,FpotIS).
flattenShapeComposition(join(S1,S2), Fjoin, Memory):- !,
	flattenShapeComposition(S1,FS1, Memory),
	flattenShapeComposition(S2,FS2, Memory),
	append(FS1,FS2,Fjoin).
flattenShapeComposition(Shapecall, FSC, Memory):-
	Shapecall  =.. [Shapeid|Args],
	Args \= [],
	shapeComposition(Shapedef,LS),
	Shapedef =.. [Shapeid|Params], !,
	substituteAllParams(LS, Params, Args, SubsLS, Memory),
	flattenShapeComposition(SubsLS, FSC, Memory).
flattenShapeComposition(N, FSC, Memory):-
	shapeComposition(N,SC), !,
	flattenShapeComposition(SC, FSC, Memory).
flattenShapeComposition(X, FSC, Memory):-
	member(var(X,SC),Memory), !,
	flattenShapeComposition(SC, FSC, Memory).

% shapeDimention(ShapeComposition,H,W, Memory)

shapeDimention(Shape,H,W,Memory) :- !,
	flattenShapeComposition(Shape,FShape,Memory),
	shapeCompositionOutsidePoints(FShape,LP),
	minMaxCoordinates(LP,Xmin, Xmax, Ymin, Ymax),
	H is Xmax-Xmin,
	W is Ymax-Ymin.

shapeCompositionOutsidePoints([],[]).
shapeCompositionOutsidePoints([s(ShapeId,T)|SCL],[[X1,Y1],[X2,Y2],[X3,Y3],[X4,Y4]|LP]):-
	shape(ShapeId,_), !, basicShapeDimention(ShapeId, H, W),
	matrixmult(T, [0, 0, 1], [X1, Y1, _]),
	matrixmult(T, [0, W, 1], [X2, Y2, _]),
	matrixmult(T, [H, 0, 1], [X3, Y3, _]),
	matrixmult(T, [H, W, 1], [X4, Y4, _]),
	shapeCompositionOutsidePoints(SCL,LP).
shapeCompositionOutsidePoints([s(ShapeId,T)|SCL],[[X1,Y1],[X2,Y2],[X3,Y3],[X4,Y4]|LP]):-
	shapeComposition(ShapeId,_), !,
	shapeDimention(s(ShapeId,T),H,W),
	matrixmult(T, [0, 0, 1], [X1, Y1, _]),
	matrixmult(T, [0, W, 1], [X2, Y2, _]),
	matrixmult(T, [H, 0, 1], [X3, Y3, _]),
	matrixmult(T, [H, W, 1], [X4, Y4, _]),
	shapeCompositionOutsidePoints(SCL,LP).
shapeCompositionOutsidePoints([ShapeId|SCL],[[X1,Y1],[X2,Y2],[X3,Y3],[X4,Y4]|LP]):-
	shapeComposition(ShapeId,_), !,
	shapeDimention(s(ShapeId,[1,0,0,0,1,0,0,0,1]),H,W),
	matrixmult(T, [0, 0, 1], [X1, Y1, _]),
	matrixmult(T, [0, W, 1], [X2, Y2, _]),
	matrixmult(T, [H, 0, 1], [X3, Y3, _]),
	matrixmult(T, [H, W, 1], [X4, Y4, _]),
	shapeCompositionOutsidePoints(SCL,LP).
shapeCompositionOutsidePoints(ShapeId,[[X1,Y1],[X2,Y2],[X3,Y3],[X4,Y4]]):-
	shapeComposition(ShapeId,_), !,
	shapeDimention(s(ShapeId,[1,0,0,0,1,0,0,0,1]),H,W),
	matrixmult(T, [0, 0, 1], [X1, Y1, _]),
	matrixmult(T, [0, W, 1], [X2, Y2, _]),
	matrixmult(T, [H, 0, 1], [X3, Y3, _]),
	matrixmult(T, [H, W, 1], [X4, Y4, _]).

minMaxCoordinates([[X,Y]],Xmin, Xmax, Ymin, Ymax) :-
	Xmin is X, Xmax is X,
	Ymin is Y, Ymax is Y.
minMaxCoordinates([[X1,Y1],[X2,Y2]|L],Xmin, Xmax, Ymin, Ymax) :-
	minMaxCoordinates([[X2,Y2]|L],Xmin2, Xmax2, Ymin2, Ymax2),
	((Xmin2>X1, !, Xmin is X1); (Xmin is Xmin2)),
	((Xmax2<X1, !, Xmax is X1); (Xmax is Xmax2)),
	((Ymin2>Y1, !, Ymin is Y1); (Ymin is Ymin2)),
	((Ymax2<Y1, !, Ymax is Y1); (Ymax is Ymax2)).

% subShape(ShapeComposition1, ShapeComposition2, Transformation)

subShape([], _, _).
subShape([s(N,T1)|L], SC, T) :-
	member(s(N,T2), SC),
	deleteShape(s(N,T2), SC, SC2),
	matrixinvert(T1, TI),
	matrixmult(T2, TI, T),
	applyTransformation(T, L, LT),
	allShapesin(LT, SC2).

allShapesin([], _).
allShapesin([X|L], K) :-
	shapesin(X, K), deleteShape(X, K, K2), allShapesin(L, K2).

shapesin(s(N,T1), [s(N,T2)|_]) :- matrixequals(T1, T2),!.
shapesin(X, [_|L]) :- shapesin(X, L).


% applyTransformation(Transformation, InitialShapeComposition, FinalShapeComposition)

applyTransformation(_, [], []).
applyTransformation(T, [s(N,TI)|LSI], [s(N,TF)|LSF]) :-
	matrixmult(T, TI, TF),
	applyTransformation(T, LSI, LSF).

% shapeBasicTransformations(Shape, Scale, Rotation, Translation)

shapeBasicTransformations(	s(N,[M11, M21, M31, M12, M22, M32, M13, M23, M33]),
				scale(XS,YS), rotation(A), translation(XT,YT)) :-
	shape(N,_),
	shapeDimention(N,H,W,[]),
	matrixmult([M11, M21, M31, M12, M22, M32, M13, M23, M33], [0, 0, 1], [A0, B0, _]),
	matrixmult([M11, M21, M31, M12, M22, M32, M13, M23, M33], [W, 0, 1], [A1, B1, _]),
	matrixmult([M11, M21, M31, M12, M22, M32, M13, M23, M33], [0, H, 1], [A2, B2, _]),
	NH is sqrt((A2-A0)*(A2-A0)+(B2-B0)*(B2-B0)),
	NW is sqrt((A1-A0)*(A1-A0)+(B1-B0)*(B1-B0)),
	XS is NW/W,
	YS is NH/H,
	Acos is acos((A1-A0)/NW)*180/pi,
	DY is B1-B0,
	((DY >= 0, A is Acos);(DY < 0, A is 360-Acos)),
	XT is A0,
	YT is B0.

% evaluate(Condition, ShapeComposition, Memory)

evaluate(not(Condition), ShapeComposition, Memory) :-
	not(evaluate(Condition, ShapeComposition, Memory)), !.

evaluate(and(Condition1,Condition2), ShapeComposition, Memory) :-
	evaluate(Condition1, ShapeComposition, Memory),
	evaluate(Condition2, ShapeComposition, Memory), !.

evaluate(or(Condition1,Condition2), ShapeComposition, Memory) :-
	((evaluate(Condition1, ShapeComposition, Memory),!);
	 evaluate(Condition2, ShapeComposition, Memory)), !.

evaluate(some(Shape, Condition), ShapeComposition, Memory) :-
	flattenShapeComposition(Shape,FSC,Memory),
	subShape(FSC, ShapeComposition, T),
	substitutefreeshape(Condition, Shape, T, SubsCondition),
	evaluate(SubsCondition, ShapeComposition, Memory), !.

evaluate(all(Shape, Condition), ShapeComposition, Memory) :-
	flattenShapeComposition(Shape,FSC,Memory),
	setof(Transformation, subShape(FSC, ShapeComposition, Transformation), LTransformation),
	evaluateAll(Condition, Shape, LTransformation, ShapeComposition, Memory), !.

evaluate(true, _, _) :- !.
evaluate(false, _, _) :- !, fail.

evaluate(Condition, ShapeComposition, Memory) :-
	Condition =.. [X|Larg],
	X \= not, X \= and, X \= or, X \= some, X \= all, X \= true, X \= false,
	listvar(Memory,LVar),
	listvalue(Memory,LValue),
	substituteAllParams(Larg, LVar, LValue, SubsLarg, []),
	Qwery =..[X,ShapeComposition, Memory|SubsLarg],
	Qwery.

evaluateAll(_, _, [], _, _).
evaluateAll(Condition, Shape, [T|LT], ShapeComposition, Memory) :-
	substitutefreeshape(Condition, Shape, T, SubsCondition),
	evaluate(SubsCondition, ShapeComposition, Memory),
	evaluateAll(Condition, Shape, LT, ShapeComposition, Memory).

% evaluateTerm(ShapeComposition, Memory, Term, ETerm)
/*
evaluateTerm(_, Memory, plus(X,Y), V) :-
	evaluateTerm(_, Memory, X, VX),
	evaluateTerm(_, Memory, Y, VY),
	V is VX + VY, !.
evaluateTerm(_, Memory, times(X,Y), V) :-
	evaluateTerm(_, Memory, X, VX),
	evaluateTerm(_, Memory, Y, VY),
	V is VX * VY, !.
evaluateTerm(_, Memory, divide(X,Y), V) :-
	evaluateTerm(_, Memory, X, VX),
	evaluateTerm(_, Memory, Y, VY),
	V is VX / VY, !.
*/
evaluateTerm(_, Memory, X, V) :-
	member(var(X,V),Memory), !.
evaluateTerm(_, _, V, V).

% substituteRelativeShapes(Condition/Process, MatchedT, SubsCondition/SubsProcess)

substituteRelativeShapes(not(Condition), MatchedT, not(SubsCondition)) :-
	substituteRelativeShapes(Condition, MatchedT, SubsCondition).
substituteRelativeShapes(and(Condition1,Condition2), MatchedT, and(SubsCondition1,SubsCondition2)) :-
	substituteRelativeShapes(Condition1, MatchedT, SubsCondition1),
	substituteRelativeShapes(Condition2, MatchedT, SubsCondition2).
substituteRelativeShapes(or(Condition1,Condition2), MatchedT, or(SubsCondition1,SubsCondition2)) :-
	substituteRelativeShapes(Condition1, MatchedT, SubsCondition1),
	substituteRelativeShapes(Condition2, MatchedT, SubsCondition2).
substituteRelativeShapes(some(Shape,Condition), MatchedT, some(Shape,SubsCondition)) :-
	substituteRelativeShapes(Condition, MatchedT, SubsCondition).
substituteRelativeShapes(all(Shape,Condition), MatchedT, all(Shape,SubsCondition)) :-
	substituteRelativeShapes(Condition, MatchedT, SubsCondition).
substituteRelativeShapes(assign(X,V), MatchedT, assign(X,SubsV)) :-
	substituteRelativeShapes(V, MatchedT, SubsV).
substituteRelativeShapes(seq(Procedure1,Procedure2), MatchedT, seq(SubsProcedure1,SubsProcedure2)) :-
	substituteRelativeShapes(Procedure1, MatchedT, SubsProcedure1),
	substituteRelativeShapes(Procedure2, MatchedT, SubsProcedure2).
substituteRelativeShapes(matched(X), MatchedT, s(X,MatchedT)).
substituteRelativeShapes(Condition, MatchedT, SubsCondition) :-
	Condition =.. [X|L],
	X \= not, X \= and, X \= or, X \= some, X \= all, X \= matched, X \= seq, X \= assign,
	substituteRelativeListMembers(L,MatchedT, SL),
	SubsCondition =.. [X|SL].

substituteRelativeListMembers([],_, []).
substituteRelativeListMembers([matched(X)|L],MatchedT, [s(X,MatchedT)|SL]):-
	substituteRelativeListMembers(L,MatchedT, SL), !.
substituteRelativeListMembers([X|L],MatchedT, [X|SL]):-
	X \= matched(_),
	substituteRelativeListMembers(L,MatchedT, SL).

% substitutefreeshape(Condition, Shape, T, SubsCondition)

substitutefreeshape(not(Condition), Shape, T, not(SubsCondition)) :-
	substitutefreeshape(Condition, Shape, T, SubsCondition).
substitutefreeshape(and(Condition1,Condition2), Shape, T, and(SubsCondition1,SubsCondition2)) :-
	substitutefreeshape(Condition1, Shape, T, SubsCondition1),
	substitutefreeshape(Condition2, Shape, T, SubsCondition2).
substitutefreeshape(or(Condition1,Condition2), Shape, T, or(SubsCondition1,SubsCondition2)) :-
	substitutefreeshape(Condition1, Shape, T, SubsCondition1),
	substitutefreeshape(Condition2, Shape, T, SubsCondition2).
substitutefreeshape(some(Shape1,Condition), Shape2, T, some(Shape2,SubsCondition)) :-
	(Shape1 = Shape2, SubsCondition = Condition, !);
	substitutefreeshape(Condition, Shape2, T, SubsCondition).
substitutefreeshape(all(Shape1,Condition), Shape2, T, all(Shape1,SubsCondition)) :-
	(Shape1 = Shape2, SubsCondition = Condition, !);
	substituteRelativeShapes(Condition, Shape2, T, SubsCondition).
substitutefreeshape(Condition, Shape, T, SubsCondition) :-
	Condition =.. [X|L],
	X \= not, X \= and, X \= or, X \= some, X \= all,
	substituteListShapes(L,Shape, T, SL),
	SubsCondition =.. [X|SL].
substituteListShapes([],_, _, []).
substituteListShapes([Shape|L],Shape, T, [s(Shape,T)|SL]) :-
	substituteListShapes(L,Shape, T, SL), !.
substituteListShapes([X|L],Shape, T, [X|SL]) :-
	X \= Shape,
	substituteListShapes(L,Shape, T, SL).

% applyRuleProcedure(Procedure, InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory)

applyRuleProcedure(pickfirst(RuleProcedure), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	applyRuleProcedure(RuleProcedure, InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory), !.

applyRuleProcedure(pickone(RuleProcedure), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	applyRuleProcedure(RuleProcedure, InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory),
	shapesbringtofront(L),
	filterby(FinalShapeComposition,L,FilteredShapeComposition,OppositeFilteredShapeComposition),
	merge(OppositeFilteredShapeComposition,FilteredShapeComposition,FilteredFinalShapeComposition),
	send(@working_shape?graphicals, for_all, if(message(@arg1, instance_of, bitmap), message(@arg1, free))),
	displayShapebyShape(FilteredFinalShapeComposition, @working_shape), send(@working_shape, flush),
	new(Msg,string('Choose an alternative solution for procedure ')), term_string(RuleProcedure,SRuleProcedure),
	send(Msg, append, SRuleProcedure),
	send(@feedback, clear), send(@feedback, insert, 0, Msg), send(@feedback, flush),
	thread_get_message(ask_more_alternatives,X),
	((X == hold,
	  delete_saved_shape,
	  assert(shapeComposition(savedShape, FinalShapeComposition)),
	  get(@selected_shape, value_set, ValueSet1),
	  send(ValueSet1,delete_all, savedShape),
	  send(ValueSet1, prepend, savedShape),
	  send(@selected_shape, value_set, ValueSet1),
	  send(@selected_shape, selection, savedShape), send(@selected_shape, flush),
	  delete_saved_memory,
	  assert(memory(savedMemory, FinalMemory)),
	  get(@selected_memory, value_set, ValueSet2),
	  send(ValueSet2,delete_all, savedMemory),
	  send(ValueSet2, prepend, savedMemory),
	  send(@selected_memory, value_set, ValueSet2),
	  send(@selected_memory, selection, savedMemory), send(@selected_memory, flush), !
	 );
	 (X == another, fail)
	).

applyRuleProcedure(pickone(RuleProcedure), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	send(@working_shape?graphicals, for_all, if(message(@arg1, instance_of, bitmap), message(@arg1, free))), send(@working_shape, flush),
	new(Msg,string('No more solutions for procedure ')), term_string(RuleProcedure,SRuleProcedure),
	send(Msg, append, SRuleProcedure), send(Msg, append, '! Try again? yes: press another'),
	send(@feedback, clear), send(@feedback, insert, 0, Msg), send(@feedback, flush),
	thread_get_message(ask_more_alternatives,X),
	((X == another, !, applyRuleProcedure(pickone(RuleProcedure), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory));
	 (FinalShapeComposition = InitialShapeComposition, FinalMemory = InitialMemory)
	).

applyRuleProcedure(or(RuleProcedure1, RuleProcedure2), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	applyRuleProcedure(RuleProcedure1, InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory);
	applyRuleProcedure(RuleProcedure2, InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory).

applyRuleProcedure(seq(RuleProcedure1, RuleProcedure2), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	applyRuleProcedure(RuleProcedure1, InitialShapeComposition, IntermediateShapeComposition, InitialMemory, IntermediateMemory),
	applyRuleProcedure(RuleProcedure2, IntermediateShapeComposition, FinalShapeComposition, IntermediateMemory, FinalMemory).

applyRuleProcedure(if(Condition, RuleProcedure1, RuleProcedure2), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	applyRuleProcedure(or(seq(verify(Condition), RuleProcedure1), seq(verify(not(Condition)), RuleProcedure2)), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory).

applyRuleProcedure(while(Condition, RuleProcedure), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	applyRuleProcedure(or(verify(not(Condition)), seq(verify(Condition),seq(RuleProcedure,while(Condition,RuleProcedure)))), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory).

applyRuleProcedure(and(RuleProcedure1, RuleProcedure2), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	serialize(and(RuleProcedure1, RuleProcedure2), SerializeRuleProcedure),
	deserialize(SerializeRuleProcedure, RuleProcedure),
	applyRuleProcedure(RuleProcedure, InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory).

applyRuleProcedure(verify(Condition), InitialShapeComposition, InitialShapeComposition, InitialMemory, InitialMemory):-
	evaluate(Condition, InitialShapeComposition, InitialMemory).

applyRuleProcedure(initialShape(ShapeComposition), _, ShapeComposition, InitialMemory, InitialMemory).

applyRuleProcedure(assign(X,V), InitialShapeComposition, InitialShapeComposition, InitialMemory, [var(X,SubsV)|FinalMemory]):-
	listvar(InitialMemory,LVar),
	listvalue(InitialMemory,LValue),
	substituteAllParams(V, LVar, LValue, SubsV, []),
	delete(var(X,_), InitialMemory, FinalMemory).

applyRuleProcedure(input(Msg,Var), InitialShapeComposition, InitialShapeComposition, InitialMemory, [var(Var,Answer)|FinalMemory]):-
	thread_send_message(ask_input,question(Msg)),
	thread_get_message(ask_input,answer(Answer)),
	delete(var(Var,_), InitialMemory, FinalMemory).

applyRuleProcedure(sr(L,R), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	applyRule(sr(L,R), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory).

applyRuleProcedure(sr(L,R,Procedure), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	applyRule(sr(L,R,Procedure), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory).

applyRuleProcedure(src(Condition,L,R), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	applyRule(src(Condition,L,R), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory).

applyRuleProcedure(src(Condition,L,R,Procedure), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	applyRule(src(Condition,L,R, Procedure), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory).

applyRuleProcedure(global(sr(L,R)), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	applyRule(srglobal(L,R), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory).

applyRuleProcedure(global(sr(L,R,P)), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	applyRule(srglobal(L,R,P), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory).

applyRuleProcedure(global(src(Condition,L,R)), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	applyRule(srcglobal(Condition,L,R), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory).

applyRuleProcedure(global(src(Condition,L,R,Procedure)), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	applyRule(srcglobal(Condition,L,R,Procedure), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory).

applyRuleProcedure(global(ShapeRulecall), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	ShapeRulecall  =.. [ShapeRuleID|Args],
	Args \= [], !,
	shapeRule(ShapeRuledef, ShapeRule),
	ShapeRuledef =.. [ShapeRuleID|Params],
	substituteAllParams(ShapeRule, Params, Args, SubsShapeRule, []),
	applyRuleProcedure(global(SubsShapeRule), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory).

applyRuleProcedure(global(ShapeRuleID), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	shapeRule(ShapeRuleID, sr(L,R)), !,
	applyRuleProcedure(global(sr(L,R)), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory).

applyRuleProcedure(on(ShapeComposition,RuleProcedure), InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	flattenShapeComposition(ShapeComposition,FShapeContext,InitialMemory),
	subShape(FShapeContext, InitialShapeComposition, _),
	deleteallShapesin(FShapeContext, InitialShapeComposition, ShapeWithoutContext),
	applyRuleProcedure(RuleProcedure,FShapeContext, FinalShapeContext, InitialMemory, FinalMemory),
	insertlist(FinalShapeContext, ShapeWithoutContext, FinalShapeComposition).

applyRuleProcedure(Procedurecall, InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	Procedurecall  =.. [Procedureid|Args],
	Args \= [], !,
	ruleProcedure(Proceduredef, RuleProcedure),
	Proceduredef =.. [Procedureid|Params],
	substituteAllParams(RuleProcedure, Params, Args, SubsRuleProcedure, []),
	applyRuleProcedure(SubsRuleProcedure, InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory).

applyRuleProcedure(Procedureid, InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory):-
	ruleProcedure(Procedureid, RuleProcedure),
	applyRuleProcedure(RuleProcedure, InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory).

applyRuleProcedure(nothing, InitialShapeComposition, InitialShapeComposition, InitialMemory, InitialMemory).

% applyRule(ShapeRule, InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory)

applyRule(sr(SCL,SCR), ISC, FSC, M, M) :-
	flattenShapeComposition(SCL,L,M),
	flattenShapeComposition(SCR,R,M),
	subShape(L, ISC, T),
	applyTransformation(T, L, LT),
	deleteallShapesin(LT, ISC, SC),
	applyTransformation(T, R, RT),
	insertlist(RT, SC, FSC).

applyRule(sr(SCL,SCR,Procedure), ISC, FSC, IM, FM) :-
	flattenShapeComposition(SCL,L,IM),
	flattenShapeComposition(SCR,R,IM),
	subShape(L, ISC, T),
	applyTransformation(T, L, LT),
	deleteallShapesin(LT, ISC, SC),
	applyTransformation(T, R, RT),
	insertlist(RT, SC, FSC),
	substituteRelativeShapes(Procedure, T, SubsProcedure),
	applyRuleProcedure(SubsProcedure, FSC, FSC, IM, FM).

applyRule(src(Condition,SCL,SCR), ISC, FSC, M, M) :-
	flattenShapeComposition(SCL,L,M),
	flattenShapeComposition(SCR,R,M),
	setof(T, subShape(L, ISC, T), LT1),
	listwithoutduplicates(LT1,LT2),
	member(MatchedT, LT2),
	substituteRelativeShapes(Condition, MatchedT, SubsCondition),
	evaluate(SubsCondition, ISC, M),
	applyTransformation(MatchedT, L, LMatchedT),
	deleteallShapesin(LMatchedT, ISC, SC),
	applyTransformation(MatchedT, R, RT),
	insertlist(RT, SC, FSC).

applyRule(src(Condition,SCL,SCR,Procedure), ISC, FSC, IM, FM) :-
	flattenShapeComposition(SCL,L,IM),
	flattenShapeComposition(SCR,R,IM),
	setof(T, subShape(L, ISC, T), LT1),
	listwithoutduplicates(LT1,LT2),
	member(MatchedT, LT2),
	substituteRelativeShapes(Condition, MatchedT, SubsCondition),
	evaluate(SubsCondition, ISC, IM),
	applyTransformation(MatchedT, L, LMatchedT),
	deleteallShapesin(LMatchedT, ISC, SC),
	applyTransformation(MatchedT, R, RT),
	insertlist(RT, SC, FSC),
	substituteRelativeShapes(Procedure, MatchedT, SubsProcedure),
	applyRuleProcedure(SubsProcedure, FSC, FSC, IM, FM).

applyRule(srglobal(SCL,SCR), ISC, FSC, M, M) :-
	flattenShapeComposition(SCL,L,M),
	flattenShapeComposition(SCR,R,M),
	setof(T, subShape(L, ISC, T), LT1),
	listwithoutduplicates(LT1,LT2),
	applyRuleforeachTransformation(sr(L,R), LT2, ISC, FSC, M, M).

applyRule(srglobal(SCL,SCR,P), ISC, FSC, IM, FM) :-
	flattenShapeComposition(SCL,L,IM),
	flattenShapeComposition(SCR,R,IM),
	setof(T, subShape(L, ISC, T), LT1),
	listwithoutduplicates(LT1,LT2),
	applyRuleforeachTransformation(sr(L,R,P), LT2, ISC, FSC, IM, FM).

applyRule(srcglobal(Condition,SCL,SCR), ISC, FSC, IM, FM) :-
	flattenShapeComposition(SCL,L,IM),
	flattenShapeComposition(SCR,R,IM),
	setof(T, subShape(L, ISC, T), LT1),
	listwithoutduplicates(LT1,LT2),
	applyRuleforeachTransformation(src(Condition,L,R), LT2, ISC, FSC, IM, FM).

applyRule(srcglobal(Condition,SCL,SCR,Procedure), ISC, FSC, IM, FM) :-
	flattenShapeComposition(SCL,L,IM),
	flattenShapeComposition(SCR,R,IM),
	setof(T, subShape(L, ISC, T), LT1),
	listwithoutduplicates(LT1,LT2),
	applyRuleforeachTransformation(src(Condition,L,R,Procedure), LT2, ISC, FSC, IM, FM).

applyRuleforeachTransformation(sr(_,_), [], ISC, ISC, M, M).
applyRuleforeachTransformation(sr(L,R), [T|W], ISC, FSC, M, M) :-
	applyTransformation(T, L, LT),
	deleteallShapesin(LT, ISC, SC),
	applyTransformation(T, R, RT),
	insertlist(RT, SC, IntSC),
	applyRuleforeachTransformation(sr(L,R), W, IntSC, FSC, M, M).
applyRuleforeachTransformation(sr(_,_,_), [], ISC, ISC, M, M).
applyRuleforeachTransformation(sr(L,R,P), [T|W], ISC, FSC, IM, FM) :-
	applyTransformation(T, L, LT),
	deleteallShapesin(LT, ISC, SC),
	applyTransformation(T, R, RT),
	insertlist(RT, SC, IntSC),
	substituteRelativeShapes(P, T, SubsP),
	applyRuleProcedure(SubsP, _, _, IM, IntM),
	applyRuleforeachTransformation(sr(L,R,P), W, IntSC, FSC, IntM, FM).

applyRuleforeachTransformation(src(_,_,_), [], ISC, ISC, M, M).
applyRuleforeachTransformation(src(Condition,L,R), [T|W], ISC, FSC, IM, FM) :-
	substituteRelativeShapes(Condition, T, SubsCondition),
	evaluate(SubsCondition, ISC, IM), !,
	applyTransformation(T, L, LT),
	deleteallShapesin(LT, ISC, SC),
	applyTransformation(T, R, RT),
	insertlist(RT, SC, IntSC),
	applyRuleforeachTransformation(src(Condition,L,R), W, IntSC, FSC, IM, FM).
applyRuleforeachTransformation(src(Condition,L,R), [_|W], ISC, FSC, IM, FM) :-
	applyRuleforeachTransformation(src(Condition,L,R), W, ISC, FSC, IM, FM).

applyRuleforeachTransformation(src(_,_,_,_), [], ISC, ISC, M, M).
applyRuleforeachTransformation(src(Condition,L,R,P), [T|W], ISC, FSC, IM, FM) :-
	substituteRelativeShapes(Condition, T, SubsCondition),
	evaluate(SubsCondition, ISC, IM), !,
	applyTransformation(T, L, LT),
	deleteallShapesin(LT, ISC, SC),
	applyTransformation(T, R, RT),
	insertlist(RT, SC, IntSC1),
	substituteRelativeShapes(P, T, SubsProcedure),
	applyRuleProcedure(SubsProcedure, IntSC1, IntSC2, IM, IntM),
	applyRuleforeachTransformation(src(Condition,L,R,P), W, IntSC2, FSC, IntM, FM).
applyRuleforeachTransformation(src(Condition,L,R,P), [_|W], ISC, FSC, IM, FM) :-
	applyRuleforeachTransformation(src(Condition,L,R,P), W, ISC, FSC, IM, FM).

deleteallShapesin([], L, L).
deleteallShapesin([X|L], K, F) :-
	deleteShape(X, K, M),
	deleteallShapesin(L, M, F).

deleteShape(_, [], []).
deleteShape(s(N,T1), [s(N,T2)|K], K) :- matrixequals(T1, T2), !.
deleteShape(X, [Y|K], [Y|M]) :- deleteShape(X,K,M).

shapesequals(s(N,T1), s(N,T2)) :- matrixequals(T1, T2).

% serialize(RuleProcedure,SerializeRuleProcedure)

serialize(seq(RuleProcedure1,RuleProcedure2),SerializeRuleProcedure) :-
	serialize(RuleProcedure1,SerializeRuleProcedure1),
	serialize(RuleProcedure2,SerializeRuleProcedure2),
	joinAll(SerializeRuleProcedure1,SerializeRuleProcedure2,SerializeRuleProcedure).

serialize(or(RuleProcedure1,RuleProcedure2),SerializeRuleProcedure) :-
	serialize(RuleProcedure1,SerializeRuleProcedure1),
	serialize(RuleProcedure2,SerializeRuleProcedure2),
	union(SerializeRuleProcedure1,SerializeRuleProcedure2,SerializeRuleProcedure).

serialize(and(RuleProcedure1,RuleProcedure2),SerializeRuleProcedure) :-
	serialize(RuleProcedure1,SerializeRuleProcedure1),
	serialize(RuleProcedure2,SerializeRuleProcedure2),
	mergeAll(SerializeRuleProcedure1,SerializeRuleProcedure2,SerializeRuleProcedure).

serialize(Procedureid, SerializeRuleProcedure):-
	ruleProcedure(Procedureid, RuleProcedure),
	serialize(RuleProcedure, SerializeRuleProcedure).

serialize(verify(Condition),[[verify(Condition)]]).

serialize(initialShape(ShapeComposition),[[initialShape(ShapeComposition)]]).

serialize(sr(L,R),[[sr(L,R)]]).

serialize(sr(L,R,P),[[sr(L,R,P)]]).

serialize(src(Condition,L,R),[[src(Condition,L,R)]]).

serialize(src(Condition,L,R,P),[[src(Condition,L,R,P)]]).

serialize(nothing,[[]]).

union([],L,L).
union([C|L1],L2,[C|L]) :- union(L1,L2,L).

joinAll([C],L1,L2) :- joinOne(C,L1,L2).
joinAll([C1,C2|L1],L2,L) :- joinAll([C2|L1],L2,L3), joinOne(C1,L2,L2C), union(L2C, L3, L).
joinOne([],L,L).
joinOne([C|L1],L2,L3) :- joinOne(L1,L2,L),puthead(C,L,L3).
puthead(C,[L],[[C|L]]).
puthead(C,[L1,L2|LT],[[C|L1]|HLT]) :- puthead(C,[L2|LT],HLT).

mergeAll([C],L1,L2) :- mergeOneAll(C,L1,L2).
mergeAll([C1,C2|L1],L2,L) :- mergeAll([C2|L1],L2,L3), mergeOneAll(C1,L2,L2C), union(L2C, L3, L).
mergeOneAll(_,[],[]).
mergeOneAll(L1,[C1|L2],L3) :- mergeOneAll(L1,L2,L), mergeOneOne(L1,C1,ML), union(L, ML, L3).
mergeOneOne([],[],[[]]).
mergeOneOne([],[X|L],[[X|L]]).
mergeOneOne([X|L],[],[[X|L]]).
mergeOneOne([X1|L1],[X2|L2],L) :-
	mergeOneOne([X1|L1],L2,L3), puthead(X2,L3,L4),
	mergeOneOne(L1,[X2|L2],L5), puthead(X1,L5,L6),
	union(L4,L6,L).

% deserialize(SerializeRuleProcedure,RuleProcedure)

deserialize([L], RP) :- deserializeOne(L, RP).
deserialize([L1,L2|LT],or(RP1,RP2)) :- deserializeOne(L1, RP1), deserialize([L2|LT], RP2).

deserializeOne([], nothing).
deserializeOne([C], C).
deserializeOne([C1,C2|L], seq(C1,RP)) :- deserializeOne([C2|L], RP).

% substituteAllParams(RuleProcedure/Condition, ListParams, ListArgs, SubsRuleProcedure/SubsCondition, Memory)

substituteAllParams(RuleProcedure, [P|Params],[A| Args], SubsRuleProcedure, Memory) :-
	substituteParam(RuleProcedure, P, A, RuleProcedure2, Memory),
	substituteAllParams(RuleProcedure2, Params, Args, SubsRuleProcedure, Memory).
substituteAllParams(RuleProcedure, [], [], RuleProcedure,_).

substituteParam(pickone(RuleProcedure), P, A, pickone(SubRuleProcedure), Memory):-
	substituteParam(RuleProcedure, P, A, SubRuleProcedure, Memory), !.
substituteParam(seq(RuleProcedure1, RuleProcedure2), P, A, seq(SubRuleProcedure1, SubRuleProcedure2), Memory):-
	substituteParam(RuleProcedure1, P, A, SubRuleProcedure1, Memory),
	substituteParam(RuleProcedure2, P, A, SubRuleProcedure2, Memory), !.
substituteParam(assign(X,V), P, A, assign(SubX,SubV), Memory):-
	substituteParam(X, P, A, SubX, []),
	substituteParam(V, P, A, SubV, Memory), !.
substituteParam(sr(L,R), P, A, sr(SubL,SubR), Memory):-
	substituteParam(L, P, A, SubL, Memory),
	substituteParam(R, P, A, SubR, Memory), !.
substituteParam(sr(L,R,Procedure), P, A, sr(SubL,SubR,SubsProcedure), Memory):-
	substituteParam(L, P, A, SubL, Memory),
	substituteParam(R, P, A, SubR, Memory),
	substituteParam(Procedure, P, A, SubsProcedure, Memory), !.
substituteParam(src(Condition,L,R), P, A, src(SubCondition,SubL,SubR), Memory):-
	substituteParam(Condition, P, A, SubCondition, Memory),
	substituteParam(L, P, A, SubL, Memory),
	substituteParam(R, P, A, SubR, Memory), !.
substituteParam(src(Condition,L,R,Procedure), P, A, src(SubCondition,SubL,SubR,SubProcedure), Memory):-
	substituteParam(Condition, P, A, SubCondition, Memory),
	substituteParam(L, P, A, SubL, Memory),
	substituteParam(R, P, A, SubR, Memory),
	substituteParam(Procedure, P, A, SubProcedure, Memory), !.
substituteParam(not(Condition), P, A,not( SubCondition), Memory):-
	substituteParam(Condition, P, A, SubCondition, Memory), !.
substituteParam(and(Condition1, Condition2), P, A, and(SubCondition1, SubCondition2), Memory):-
	substituteParam(Condition1, P, A, SubCondition1, Memory),
	substituteParam(Condition2, P, A, SubCondition2, Memory), !.
substituteParam(or(Condition1, Condition2), P, A, or(SubCondition1, SubCondition2), Memory):-
	substituteParam(Condition1, P, A, SubCondition1, Memory),
	substituteParam(Condition2, P, A, SubCondition2, Memory), !.
substituteParam(some(Shape, Condition), P, A, some(SubShape, SubCondition), Memory):-
	substituteParam(Shape, P, A, SubShape, Memory),
	substituteParam(Condition, P, A, SubCondition, Memory), !.
substituteParam(all(Shape, Condition), P, A, all(SubShape, SubCondition), Memory):-
	substituteParam(Shape, P, A, SubShape, Memory),
	substituteParam(Condition, P, A, SubCondition, Memory), !.
substituteParam(matched(Shape), P, A,matched(SubShape), Memory):-
	substituteParam(Shape, P, A, SubShape, Memory), !.
substituteParam([X|L], P, A,[SubX|SubL], Memory):-
	substituteParam(X, P, A, SubX, Memory),
	substituteParam(L, P, A, SubL, Memory),!.
substituteParam([], _, _,[],_):- !.
substituteParam(Condition, P, A, SubCondition, Memory):-
	Condition =.. [X|L], L \= [],
	substituteParam(L,P, A, SL, Memory),
	SubCondition =.. [X|SL], !.

% substituteParam(P, P, A, V, Memory) :-
%	member(var(A,V), Memory), !.

substituteParam(P, P, A, V, Memory) :-
	evaluateTerm(_, Memory, A, V), !.
substituteParam(P, P, A, A, _) :- !.
substituteParam(X, _, _, X, _).

/*
============================
   Predicates
============================
*/


% next(ShapeComposition, Memory, Shapeid1, Shapeid2)

next(_, Memory, Shape1, Shape2) :-
	next(Memory, Shape1, Shape2).

% next(Memory, Shape1, Shape2)

next(Memory, Shape1, Shape2) :-
	EM is 0.000001,
	flattenShapeComposition(Shape1,FShape1,Memory),
	shapeCompositionOutsidePoints(FShape1,LP1),
	minMaxCoordinates(LP1,X1min, X1max, Y1min, Y1max),
	flattenShapeComposition(Shape2,FShape2,Memory),
	shapeCompositionOutsidePoints(FShape2,LP2),
	minMaxCoordinates(LP2,X2min, X2max, Y2min, Y2max),
	( ( ((abs(X1max-X2min) =< EM);(abs(X2max-X1min) =< EM)),
	    not(Y2min+EM > Y1max), not(Y2max < Y1min+EM)
	  );
	  ( ((abs(Y2max-Y1min) =< EM);(abs(Y1max-Y2min) =< EM)),
	    not(X2min+EM > X1max), not(X2max < X1min+EM)
	  )
	).

% overlapped(ShapeComposition, Memory, Shape1, Shape2)

overlapped(_, Memory, Shape1, Shape2) :-
	overlapped(Memory, Shape1, Shape2).

% overlapped(Memory, Shape1, Shape2)

overlapped(Memory, Shape1, Shape2):-
	EM is 0.000001,
	flattenShapeComposition(Shape1,FShape1,Memory),
	shapeCompositionOutsidePoints(FShape1,LP1),
	minMaxCoordinates(LP1,X1min, X1max, Y1min, Y1max),
	flattenShapeComposition(Shape2,FShape2,Memory),
	shapeCompositionOutsidePoints(FShape2,LP2),
	minMaxCoordinates(LP2,X2min, X2max, Y2min, Y2max),
	((X1min < X2min, X2min < X1max, X1max-X2min > EM);(X2min =< X1min, X1min < X2max, X2max-X1min > EM)),
	((Y1min < Y2min, Y2min < Y1max, Y1max-Y2min > EM);(Y2min =< Y1min, Y1min < Y2max, Y2max-Y1min > EM)).

% v/hhmiddle(ShapeComposition, Memory, Shape1, Shape2)

vmiddle(_, Memory, Shape1, Shape2) :-
	vmiddle(Memory, Shape1, Shape2).

hmiddle(_, Memory, Shape1, Shape2) :-
	hmiddle(Memory, Shape1, Shape2).

% v/hmiddle(Memory, Shape1, Shape2)

vmiddle(Memory, Shape1, Shape2):-
	EM is 0.000001,
	flattenShapeComposition(Shape1,FShape1,Memory),
	shapeCompositionOutsidePoints(FShape1,LP1),
	minMaxCoordinates(LP1, _, _, Y1min, Y1max),
	flattenShapeComposition(Shape2,FShape2,Memory),
	shapeCompositionOutsidePoints(FShape2,LP2),
	minMaxCoordinates(LP2, _, _, Y2min, Y2max),
	Y1middle is (Y1max+Y1min)/2, Y2middle is (Y2max+Y2min)/2,
	abs(Y1middle-Y2middle - EM)  < 0.3.

hmiddle(Memory, Shape1, Shape2):-
	EM is 0.000001,
	flattenShapeComposition(Shape1,FShape1,Memory),
	shapeCompositionOutsidePoints(FShape1,LP1),
	minMaxCoordinates(LP1,X1min, X1max, _, _),
	flattenShapeComposition(Shape2,FShape2,Memory),
	shapeCompositionOutsidePoints(FShape2,LP2),
	minMaxCoordinates(LP2,X2min, X2max, _, _),
	X1middle is (X1max+X1min)/2, X2middle is (X2max+X2min)/2,
	abs(X1middle - X2middle - EM) < 0.3.

% equals(ShapeComposition, Memory, Term1, Term2) :-

equals(ShapeComposition, Memory, Term1, Term2) :-
	evaluateTerm(ShapeComposition, Memory, Term1, ETerm1),
	evaluateTerm(ShapeComposition, Memory, Term2, ETerm2),
	ETerm1 == ETerm2.

% allconected(ShapeComposition, Memory, Shapeid)

allconnected(ShapeComposition, _, Shapeid) :-
	allconnected(ShapeComposition, Shapeid).

% allconected(ShapeComposition, Shapeid)

allconnected(ShapeComposition, Shapeid) :-
	member(s(Shapeid,T), ShapeComposition),
	connected_closure_usingshapes(ShapeComposition, [s(Shapeid,T)], [Shapeid], C),
	deleteallShapesin(C, ShapeComposition, ShapeCompositionCout), !,
	not(member(s(Shapeid,_), ShapeCompositionCout)).

% connected_usingshapes(ShapeComposition, Shape1, Shape2, L)

connected_using_shapes(ShapeComposition, Shape1, s(Shapeid2,T), L) :-
	connected_closure_usingshapes(ShapeComposition, [Shape1], [Shapeid2|L], C),
	member(s(Shapeid2,T), C).
connected_closure_usingshapes(_, [], _, []).
connected_closure_usingshapes(ShapeComposition, [X|K], L, C2) :-
	setof(s(S,T), (member(s(S,T), ShapeComposition), member(Shape,[X|K]), next(s(S,T),Shape), member(S,L)), ShapeCompositionNext),
	deleteallShapesin(ShapeCompositionNext, ShapeComposition, ShapeCompositionNextOut),
	connected_closure_usingshapes(ShapeCompositionNextOut, ShapeCompositionNext, L, C1),
	insertlist(ShapeCompositionNext, C1, C2).

/*
============================
   Matrix Calculation
============================
*/

matrixmult(	[M11, M21, M31, M12, M22, M32, M13, M23, M33],
		[N11, N21, N31, N12, N22, N32, N13, N23, N33],
		[O11, O21, O31, O12, O22, O32, O13, O23, O33]) :-
	O11 is M11*N11 + M12*N21 + M13*N31,
	O21 is M21*N11 + M22*N21 + M23*N31,
	O31 is M31*N11 + M32*N21 + M33*N31,
	O12 is M11*N12 + M12*N22 + M13*N32,
	O22 is M21*N12 + M22*N22 + M23*N32,
	O32 is M31*N12 + M32*N22 + M33*N32,
	O13 is M11*N13 + M12*N23 + M13*N33,
	O23 is M21*N13 + M22*N23 + M23*N33,
	O33 is M31*N13 + M32*N23 + M33*N33.

matrixmult(	[M11, M21, M31, M12, M22, M32, M13, M23, M33],
		[N11, N21, N31],
		[O11, O21, O31]) :-
	O11 is M11*N11 + M12*N21 + M13*N31,
	O21 is M21*N11 + M22*N21 + M23*N31,
	O31 is M31*N11 + M32*N21 + M33*N31.

matrixpot(_, 0, [1, 0, 0, 0, 1, 0, 0, 0, 1]) :-!.
matrixpot(T, N, TPN) :-
	N > 0, !,
	M is N-1,
	matrixpot(T, M, TPM),
	matrixmult(T, TPM, TPN).

matrixinvert(	[M11, M21, M31, M12, M22, M32, M13, M23, M33],
		[N11, N21, N31, N12, N22, N32, N13, N23, N33]) :-
	DET is M11*(M22*M33-M23*M32)-M12*(M33*M21-M23*M31)+M13*(M21*M32-M22*M32),
	N11 is (M22*M33-M23*M32)/DET,
	N21 is -(M21*M33-M23*M31)/DET,
	N31 is (M21*M32-M22*M31)/DET,
	N12 is -(M12*M33-M13*M32)/DET,
	N22 is (M11*M33-M13*M31)/DET,
	N32 is -(M11*M32-M12*M31)/DET,
	N13 is (M12*M23-M13*M22)/DET,
	N23 is -(M11*M23-M13*M21)/DET,
	N33 is (M11*M22-M12*M21)/DET.

matrixequals(	[M11, M21, M31, M12, M22, M32, M13, M23, M33],
		[N11, N21, N31, N12, N22, N32, N13, N23, N33]) :-
	abs(M11-N11) =< 0.00001,
	abs(M21-N21) =< 0.00001,
	abs(M31-N31) =< 0.00001,
	abs(M12-N12) =< 0.00001,
	abs(M22-N22) =< 0.00001,
	abs(M32-N32) =< 0.00001,
	abs(M13-N13) =< 0.00001,
	abs(M23-N23) =< 0.00001,
	abs(M33-N33) =< 0.00001.

matrixmember(X,[Y|_]) :-
	matrixequals(X,Y), !.
matrixmember(X,[_|L]) :-
	matrixmember(X,L).

listwithoutduplicates([],[]).
listwithoutduplicates([X|L1],L2) :-
	matrixmember(X,L1), !,
	listwithoutduplicates(L1,L2).
listwithoutduplicates([X|L1],[X|L2]) :-
	listwithoutduplicates(L1,L2).

/*
============================
   List Predicates
============================
*/

deletelist([], L, L).
deletelist([X|L], K, F) :-
	delete(X, K, M),
	deletelist(L, M, F).

delete(_, [], []).
delete(X, [X|K], K) :- !.
delete(X, [Y|K], [Y|M]) :- delete(X,K,M).

insertlist([], L, L).
insertlist([X|L], K, [X|F]) :- insertlist(L, K, F).

memberlist([], _).
memberlist([X|L], K) :-
	member(X, K), memberlist(L, K).

member(X, [X|_]).
member(X, [_|L]) :- member(X, L).

times([],_,0).
times([X|L], Y, N) :-
	unifiable(X, Y, _),
	times(L, Y, M),
	N is M+1.
times([X|L], Y, N) :-
	not(unifiable(X, Y, _)),
	times(L, Y, N).